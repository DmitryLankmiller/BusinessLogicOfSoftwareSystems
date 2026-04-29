package ru.ifmo.se.lab.service.booking;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import ru.ifmo.se.lab.dto.BookingRequestDto;
import ru.ifmo.se.lab.dto.DtoMapper;
import ru.ifmo.se.lab.dto.PageResponse;
import ru.ifmo.se.lab.exception.ResourceNotFoundException;
import ru.ifmo.se.lab.model.Accommodation;
import ru.ifmo.se.lab.model.AppRole;
import ru.ifmo.se.lab.model.BookingRequest;
import ru.ifmo.se.lab.model.PaymentData;
import ru.ifmo.se.lab.model.User;
import ru.ifmo.se.lab.repository.AccommodationRepository;
import ru.ifmo.se.lab.repository.BookingRequestRepository;
import ru.ifmo.se.lab.repository.PaymentDataRepository;
import ru.ifmo.se.lab.repository.UserRepository;
import ru.ifmo.se.lab.security.AccessService;
import ru.ifmo.se.lab.security.SecurityUtils;

@Service
@RequiredArgsConstructor
public class BookingRequestCrudService {

    private final BookingRequestRepository bookingRequestRepository;
    private final AccommodationRepository accommodationRepository;
    private final UserRepository userRepository;
    private final PaymentDataRepository paymentDataRepository;
    private final AccessService accessService;

    public PageResponse<BookingRequestDto> findBookingRequests(
            int page,
            int size,
            String sortBy,
            String sortDir) {
        var principal = SecurityUtils.getCurrentPrincipal();
        if (principal.getRole() == AppRole.ROLE_ADMIN) {
            Sort sort = buildSort(sortBy, sortDir);
            Page<BookingRequest> bookingRequests = bookingRequestRepository.findAll(PageRequest.of(page, size, sort));
            return buildBookingRequestPageResponse(bookingRequests);
        }

        if (principal.getRole() == AppRole.ROLE_USER) {
            List<BookingRequest> bookingRequests = bookingRequestRepository.findAllByClientId(principal.getId());
            List<BookingRequest> sorted = sortBookingRequests(bookingRequests, sortBy, sortDir);
            return buildBookingRequestPageResponse(sorted, page, size);
        }

        if (principal.getRole() == AppRole.ROLE_HOST) {
            List<BookingRequest> bookingRequests = bookingRequestRepository.findAllByHostId(principal.getId());
            List<BookingRequest> sorted = sortBookingRequests(bookingRequests, sortBy, sortDir);
            return buildBookingRequestPageResponse(sorted, page, size);
        }

        throw new AccessDeniedException("Access denied");
    }

    public PageResponse<BookingRequestDto> findBookingRequestsByAccommodation(
            int accommodationId,
            int page,
            int size,
            String sortBy,
            String sortDir) {
        List<BookingRequest> bookingRequests;

        var principal = SecurityUtils.getCurrentPrincipal();
        if (principal.getRole() == AppRole.ROLE_ADMIN) {
            bookingRequests = bookingRequestRepository.findAllByAccommodationId(accommodationId);
        } else if (principal.getRole() == AppRole.ROLE_HOST) {
            bookingRequests = bookingRequestRepository.findAllByAccommodationIdAndHostId(accommodationId,
                    principal.getId());
        } else {
            throw new AccessDeniedException("Access denied");
        }

        List<BookingRequest> sorted = sortBookingRequests(bookingRequests, sortBy, sortDir);
        return buildBookingRequestPageResponse(sorted, page, size);
    }

    public BookingRequestDto findBookingRequestById(int id) {
        BookingRequest bookingRequest = bookingRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "BookingRequest with id=%d not found".formatted(id)));

        var principal = SecurityUtils.getCurrentPrincipal();
        accessService.checkBookingRequestReadAccess(principal, bookingRequest);
        return DtoMapper.toDto(bookingRequest);
    }

    public BookingRequestDto addBookingRequest(BookingRequestDto bookingRequestDto) {
        var principal = SecurityUtils.getCurrentPrincipal();
        if (principal.getRole() != AppRole.ROLE_ADMIN && principal.getRole() != AppRole.ROLE_USER) {
            throw new AccessDeniedException("Access denied");
        }

        if (principal.getRole() == AppRole.ROLE_USER && principal.getId() != bookingRequestDto.getClientId()) {
            throw new AccessDeniedException("Access denied");
        }

        Accommodation accommodation = accommodationRepository.findById(bookingRequestDto.getAccommodationId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Accommodation with id=%d not found".formatted(bookingRequestDto.getAccommodationId())));

        User client = userRepository.findById(bookingRequestDto.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with id=%d not found".formatted(bookingRequestDto.getClientId())));

        User host = userRepository.findById(bookingRequestDto.getHostId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with id=%d not found".formatted(bookingRequestDto.getHostId())));

        PaymentData paymentData = paymentDataRepository.findById(bookingRequestDto.getPaymentDataId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "PaymentData with id=%d not found".formatted(bookingRequestDto.getPaymentDataId())));

        BookingRequest bookingRequest = BookingRequest.builder()
                .accommodation(accommodation)
                .client(client)
                .host(host)
                .paymentData(paymentData)
                .checkIn(bookingRequestDto.getCheckIn())
                .checkOut(bookingRequestDto.getCheckOut())
                .messageToHost(bookingRequestDto.getMessageToHost())
                .build();

        BookingRequest savedBookingRequest = bookingRequestRepository.save(bookingRequest);
        return DtoMapper.toDto(savedBookingRequest);
    }

    public BookingRequestDto updateBookingRequest(int id, BookingRequestDto bookingRequestDto) {
        BookingRequest bookingRequest = bookingRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "BookingRequest with id=%d not found".formatted(id)));

        var principal = SecurityUtils.getCurrentPrincipal();
        if (principal.getRole() == AppRole.ROLE_USER) {
            if (bookingRequest.getClient().getId() != principal.getId()
                    || bookingRequestDto.getClientId() != principal.getId()) {
                throw new AccessDeniedException("Access denied");
            }
        } else if (principal.getRole() != AppRole.ROLE_ADMIN) {
            throw new AccessDeniedException("Access denied");
        }

        Accommodation accommodation = accommodationRepository.findById(bookingRequestDto.getAccommodationId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Accommodation with id=%d not found".formatted(bookingRequestDto.getAccommodationId())));

        User client = userRepository.findById(bookingRequestDto.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with id=%d not found".formatted(bookingRequestDto.getClientId())));

        User host = userRepository.findById(bookingRequestDto.getHostId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with id=%d not found".formatted(bookingRequestDto.getHostId())));

        PaymentData paymentData = paymentDataRepository.findById(bookingRequestDto.getPaymentDataId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "PaymentData with id=%d not found".formatted(bookingRequestDto.getPaymentDataId())));

        bookingRequest.setAccommodation(accommodation);
        bookingRequest.setClient(client);
        bookingRequest.setHost(host);
        bookingRequest.setPaymentData(paymentData);
        bookingRequest.setCheckIn(bookingRequestDto.getCheckIn());
        bookingRequest.setCheckOut(bookingRequestDto.getCheckOut());
        bookingRequest.setMessageToHost(bookingRequestDto.getMessageToHost());

        BookingRequest savedBookingRequest = bookingRequestRepository.save(bookingRequest);
        return DtoMapper.toDto(savedBookingRequest);
    }

    public void deleteBookingRequest(int id) {
        BookingRequest bookingRequest = bookingRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "BookingRequest with id=%d not found".formatted(id)));

        var principal = SecurityUtils.getCurrentPrincipal();
        if (principal.getRole() == AppRole.ROLE_USER) {
            if (bookingRequest.getClient().getId() != principal.getId()) {
                throw new AccessDeniedException("Access denied");
            }
        } else if (principal.getRole() != AppRole.ROLE_ADMIN) {
            throw new AccessDeniedException("Access denied");
        }

        bookingRequestRepository.delete(bookingRequest);
    }

    private PageResponse<BookingRequestDto> buildBookingRequestPageResponse(Page<BookingRequest> bookingRequests) {
        List<BookingRequestDto> content = bookingRequests.getContent().stream()
                .map(DtoMapper::toDto)
                .collect(Collectors.toList());

        PageResponse<BookingRequestDto> response = new PageResponse<>();
        response.setContent(content);
        response.setPage(bookingRequests.getNumber());
        response.setSize(bookingRequests.getSize());
        response.setTotalElements(bookingRequests.getTotalElements());
        response.setTotalPages(bookingRequests.getTotalPages());
        response.setHasNext(bookingRequests.hasNext());
        return response;
    }

    private PageResponse<BookingRequestDto> buildBookingRequestPageResponse(
            List<BookingRequest> bookingRequests,
            int page,
            int size) {
        int fromIndex = Math.min(page * size, bookingRequests.size());
        int toIndex = Math.min(fromIndex + size, bookingRequests.size());

        List<BookingRequestDto> content = bookingRequests.subList(fromIndex, toIndex).stream()
                .map(DtoMapper::toDto)
                .toList();

        PageResponse<BookingRequestDto> response = new PageResponse<>();
        response.setContent(content);
        response.setPage(page);
        response.setSize(size);
        response.setTotalElements((long) bookingRequests.size());
        response.setTotalPages((int) Math.ceil((double) bookingRequests.size() / size));
        response.setHasNext(toIndex < bookingRequests.size());
        return response;
    }

    private Sort buildSort(String sortBy, String sortDir) {
        return sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
    }

    private List<BookingRequest> sortBookingRequests(List<BookingRequest> bookingRequests, String sortBy,
            String sortDir) {
        Comparator<BookingRequest> comparator = comparatorForBookingRequest(sortBy);
        if (!sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())) {
            comparator = comparator.reversed();
        }

        return bookingRequests.stream()
                .sorted(comparator)
                .toList();
    }

    private Comparator<BookingRequest> comparatorForBookingRequest(String sortBy) {
        return switch (sortBy) {
            case "accommodationId", "accommodation_id" ->
                Comparator.comparingInt(request -> request.getAccommodation().getId());
            case "clientId", "client_id" -> Comparator.comparingInt(request -> request.getClient().getId());
            case "hostId", "host_id" -> Comparator.comparingInt(request -> request.getHost().getId());
            case "paymentDataId", "payment_data_id" ->
                Comparator.comparingInt(request -> request.getPaymentData().getId());
            case "checkIn", "check_in" -> Comparator.comparing(BookingRequest::getCheckIn);
            case "checkOut", "check_out" -> Comparator.comparing(BookingRequest::getCheckOut);
            case "messageToHost", "message_to_host" -> Comparator.comparing(
                    BookingRequest::getMessageToHost,
                    String.CASE_INSENSITIVE_ORDER);
            default -> Comparator.comparingInt(BookingRequest::getId);
        };
    }
}