package ru.ifmo.se.lab.service.booking;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionTemplate;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.ifmo.se.lab.dto.BookingRequestCreateDto;
import ru.ifmo.se.lab.dto.BookingRequestDto;
import ru.ifmo.se.lab.dto.DtoMapper;
import ru.ifmo.se.lab.exception.ConflictException;
import ru.ifmo.se.lab.exception.ResourceNotFoundException;
import ru.ifmo.se.lab.model.Accommodation;
import ru.ifmo.se.lab.model.AppRole;
import ru.ifmo.se.lab.model.Booking;
import ru.ifmo.se.lab.model.BookingRequest;
import ru.ifmo.se.lab.model.PaymentConfirmation;
import ru.ifmo.se.lab.model.PaymentData;
import ru.ifmo.se.lab.model.User;
import ru.ifmo.se.lab.repository.AccommodationRepository;
import ru.ifmo.se.lab.repository.BookingRepository;
import ru.ifmo.se.lab.repository.BookingRequestRepository;
import ru.ifmo.se.lab.repository.PaymentConfirmationRepository;
import ru.ifmo.se.lab.repository.UserRepository;
import ru.ifmo.se.lab.security.AccessService;
import ru.ifmo.se.lab.security.SecurityUtils;
import ru.ifmo.se.lab.service.mail.MailService;
import ru.ifmo.se.lab.service.payment.PaymentService;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingOrchestrationService {

    private static final long HOST_RESPONSE_TIMEOUT_HOURS = 1L;

    private final BookingRequestRepository bookingRequestRepository;
    private final BookingRepository bookingRepository;
    private final AccommodationRepository accommodationRepository;
    private final UserRepository userRepository;
    private final PaymentConfirmationRepository paymentConfirmationRepository;
    private final PaymentService paymentService;
    private final MailService mailService;
    private final AccessService accessService;

    private final ApplicationEventPublisher eventPublisher;
    private final TransactionTemplate transactionTemplate;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final Map<Integer, ScheduledFuture<?>> bookingRequestTimeouts = new ConcurrentHashMap<>();

    public BookingRequestDto createBookingRequest(
            BookingRequestCreateDto bookingRequestCreateDto) {
        var principal = SecurityUtils.getCurrentPrincipal();
        if (principal.getRole() != AppRole.ROLE_ADMIN && principal.getRole() != AppRole.ROLE_USER) {
            throw new AccessDeniedException("Access denied");
        }

        if (principal.getRole() == AppRole.ROLE_USER && principal.getId() != bookingRequestCreateDto.getClientId()) {
            throw new AccessDeniedException("Access denied");
        }

        Accommodation accommodation = accommodationRepository.findById(bookingRequestCreateDto.getAccommodationId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Accommodation with id=%d not found".formatted(bookingRequestCreateDto.getAccommodationId())));

        User client = userRepository.findById(bookingRequestCreateDto.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with id=%d not found".formatted(bookingRequestCreateDto.getClientId())));

        User host = accommodation.getHost();

        validateDates(bookingRequestCreateDto.getCheckIn(), bookingRequestCreateDto.getCheckOut());
        validateAccommodationAvailability(
                accommodation.getId(),
                bookingRequestCreateDto.getCheckIn(),
                bookingRequestCreateDto.getCheckOut());

        long nights = ChronoUnit.DAYS.between(
                bookingRequestCreateDto.getCheckIn(),
                bookingRequestCreateDto.getCheckOut());
        long amount = nights * accommodation.getPricePerNight();

        return transactionTemplate.execute((status) -> {
            PaymentData paymentData = paymentService.executeHold(
                    client,
                    amount,
                    "Booking request for accommodation %d".formatted(accommodation.getId()),
                    bookingRequestCreateDto.getPaymentInputInfo());
            eventPublisher.publishEvent(new BookingHoldExecutedEvent(this, paymentData));

            BookingRequest bookingRequest = BookingRequest.builder()
                    .accommodation(accommodation)
                    .client(client)
                    .host(host)
                    .paymentData(paymentData)
                    .checkIn(bookingRequestCreateDto.getCheckIn())
                    .checkOut(bookingRequestCreateDto.getCheckOut())
                    .messageToHost(bookingRequestCreateDto.getMessageToHost())
                    .build();

            BookingRequest savedBookingRequest = bookingRequestRepository.save(bookingRequest);

            mailService.sendBookingRequestToHost(savedBookingRequest);

            scheduleTimeout(savedBookingRequest.getId());

            return DtoMapper.toDto(savedBookingRequest);
        });
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void handleBookingRequestFailed(BookingHoldExecutedEvent holdExecutedEvent) {
        log.warn("Cancel payment hold with id=%d".formatted(holdExecutedEvent.getPaymentData().getId()));
        paymentService.handleCancel(holdExecutedEvent.getPaymentData());
    }

    public void resolveBookingRequest(int bookingRequestId, boolean confirm, String reason) {
        BookingRequest bookingRequest = bookingRequestRepository.findById(bookingRequestId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "BookingRequest with id=%d not found".formatted(bookingRequestId)));

        var principal = SecurityUtils.getCurrentPrincipal();
        accessService.checkBookingRequestResolveAccess(principal, bookingRequest);
        cancelTimeout(bookingRequestId);

        boolean alreadyConfirmed = paymentConfirmationRepository.findAll().stream()
                .anyMatch(paymentConfirmation -> paymentConfirmation.getPaymentData().getId() == bookingRequest
                        .getPaymentData().getId());

        if (alreadyConfirmed) {
            throw new ConflictException("Booking request is already confirmed");
        }

        if (confirm) {
            validateAccommodationAvailability(
                    bookingRequest.getAccommodation().getId(),
                    bookingRequest.getCheckIn(),
                    bookingRequest.getCheckOut());

            long price = calculatePrice(bookingRequest);

            Booking booking = Booking.builder()
                    .accommodation(bookingRequest.getAccommodation())
                    .user(bookingRequest.getClient())
                    .checkIn(bookingRequest.getCheckIn())
                    .checkOut(bookingRequest.getCheckOut())
                    .price(price)
                    .build();

            transactionTemplate.executeWithoutResult(status -> {
                Booking savedBooking = bookingRepository.save(booking);

                paymentConfirmationRepository.save(
                        PaymentConfirmation.builder()
                                .paymentData(bookingRequest.getPaymentData())
                                .booking(savedBooking)
                                .build());

                mailService.sendBookingConfirmedToClient(savedBooking);
                eventPublisher.publishEvent(new BookingConfirmedEmailSentEvent(this, savedBooking.getId(),
                        savedBooking.getUser().getEmail(), savedBooking.getAccommodation().getId()));

                // throw new RuntimeException("Test capture failed");
                paymentService.handleCapture(bookingRequest.getPaymentData());
            });

            return;
        }

        paymentService.handleCancel(bookingRequest.getPaymentData());
        mailService.sendBookingRejectedToClient(bookingRequest, reason);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void handleCaptureFailed(BookingConfirmedEmailSentEvent captureFailedEvent) {
        log.warn("Capture failed for booking with id=%d".formatted(captureFailedEvent.getBookingId()));
        mailService.sendCaptureFailedToClient(captureFailedEvent.getBookingId(), captureFailedEvent.getUserEmail(),
                captureFailedEvent.getAccommodationId());
    }

    @Transactional
    public void handleTimeout(int bookingRequestId) {
        BookingRequest bookingRequest = bookingRequestRepository.findById(bookingRequestId).orElse(null);
        if (bookingRequest == null) {
            bookingRequestTimeouts.remove(bookingRequestId);
            return;
        }

        boolean alreadyConfirmed = paymentConfirmationRepository.findAll().stream()
                .anyMatch(paymentConfirmation -> paymentConfirmation.getPaymentData().getId() == bookingRequest
                        .getPaymentData().getId());

        if (alreadyConfirmed) {
            bookingRequestTimeouts.remove(bookingRequestId);
            return;
        }

        paymentService.handleCancel(bookingRequest.getPaymentData());
        mailService.sendBookingExpiredToClient(bookingRequest);
        bookingRequestTimeouts.remove(bookingRequestId);
    }

    @PreDestroy
    public void shutdown() {
        scheduler.shutdownNow();
    }

    private void scheduleTimeout(int bookingRequestId) {
        ScheduledFuture<?> previousFuture = bookingRequestTimeouts.remove(bookingRequestId);
        if (previousFuture != null) {
            previousFuture.cancel(false);
        }

        ScheduledFuture<?> scheduledFuture = scheduler.schedule(
                () -> handleTimeout(bookingRequestId),
                HOST_RESPONSE_TIMEOUT_HOURS,
                TimeUnit.HOURS);
        bookingRequestTimeouts.put(bookingRequestId, scheduledFuture);
    }

    private void cancelTimeout(int bookingRequestId) {
        ScheduledFuture<?> scheduledFuture = bookingRequestTimeouts.remove(bookingRequestId);
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
        }
    }

    private void validateDates(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException("Invalid booking dates");
        }
    }

    private void validateAccommodationAvailability(int accommodationId, LocalDate checkIn, LocalDate checkOut) {
        boolean hasIntersections = !bookingRepository
                .findAllByAccommodationIdAndCheckInLessThanAndCheckOutGreaterThan(
                        accommodationId,
                        checkOut,
                        checkIn)
                .isEmpty();

        if (hasIntersections) {
            throw new ConflictException("Accommodation is not available for the selected dates");
        }
    }

    private long calculatePrice(BookingRequest bookingRequest) {
        long nights = ChronoUnit.DAYS.between(
                bookingRequest.getCheckIn(),
                bookingRequest.getCheckOut());
        return nights * bookingRequest.getAccommodation().getPricePerNight();
    }
}