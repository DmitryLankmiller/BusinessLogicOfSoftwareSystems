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
import ru.ifmo.se.lab.dto.BookingDto;
import ru.ifmo.se.lab.dto.DtoMapper;
import ru.ifmo.se.lab.dto.PageResponse;
import ru.ifmo.se.lab.exception.ResourceNotFoundException;
import ru.ifmo.se.lab.model.Accommodation;
import ru.ifmo.se.lab.model.AppRole;
import ru.ifmo.se.lab.model.Booking;
import ru.ifmo.se.lab.model.User;
import ru.ifmo.se.lab.repository.AccommodationRepository;
import ru.ifmo.se.lab.repository.BookingRepository;
import ru.ifmo.se.lab.repository.UserRepository;
import ru.ifmo.se.lab.security.AccessService;
import ru.ifmo.se.lab.security.SecurityUtils;

@Service
@RequiredArgsConstructor
public class BookingCrudService {

    private final BookingRepository bookingRepository;
    private final AccommodationRepository accommodationRepository;
    private final UserRepository userRepository;
    private final AccessService accessService;

    public PageResponse<BookingDto> findBookings(
            int page,
            int size,
            String sortBy,
            String sortDir) {
        var principal = SecurityUtils.getCurrentPrincipal();
        if (principal.getRole() == AppRole.ROLE_ADMIN) {
            Sort sort = buildSort(sortBy, sortDir);
            Page<Booking> bookings = bookingRepository.findAll(PageRequest.of(page, size, sort));
            return buildBookingPageResponse(bookings);
        }

        if (principal.getRole() == AppRole.ROLE_USER) {
            List<Booking> bookings = bookingRepository.findAllByUserId(principal.getId());
            List<Booking> sorted = sortBookings(bookings, sortBy, sortDir);
            return buildBookingPageResponse(sorted, page, size);
        }

        if (principal.getRole() == AppRole.ROLE_HOST) {
            List<Booking> bookings = bookingRepository.findAllByAccommodationHostId(principal.getId());
            List<Booking> sorted = sortBookings(bookings, sortBy, sortDir);
            return buildBookingPageResponse(sorted, page, size);
        }

        throw new AccessDeniedException("Access denied");
    }

    public PageResponse<BookingDto> findBookingsByAccommodation(
            int accommodationId,
            int page,
            int size,
            String sortBy,
            String sortDir) {
        List<Booking> bookings;

        var principal = SecurityUtils.getCurrentPrincipal();
        if (principal.getRole() == AppRole.ROLE_ADMIN) {
            bookings = bookingRepository.findAllByAccommodationId(accommodationId);
        } else if (principal.getRole() == AppRole.ROLE_HOST) {
            bookings = bookingRepository.findAllByAccommodationIdAndAccommodationHostId(accommodationId,
                    principal.getId());
        } else {
            throw new AccessDeniedException("Access denied");
        }

        List<Booking> sorted = sortBookings(bookings, sortBy, sortDir);
        return buildBookingPageResponse(sorted, page, size);
    }

    public BookingDto findBookingById(int id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking with id=%d not found".formatted(id)));

        var principal = SecurityUtils.getCurrentPrincipal();
        accessService.checkBookingReadAccess(principal, booking);
        return DtoMapper.toDto(booking);
    }

    public BookingDto addBooking(BookingDto bookingDto) {
        var principal = SecurityUtils.getCurrentPrincipal();
        if (principal.getRole() != AppRole.ROLE_ADMIN) {
            throw new AccessDeniedException("Access denied");
        }

        Accommodation accommodation = accommodationRepository.findById(bookingDto.getAccommodationId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Accommodation with id=%d not found".formatted(bookingDto.getAccommodationId())));

        User user = userRepository.findById(bookingDto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with id=%d not found".formatted(bookingDto.getUserId())));

        Booking booking = Booking.builder()
                .accommodation(accommodation)
                .user(user)
                .checkIn(bookingDto.getCheckIn())
                .checkOut(bookingDto.getCheckOut())
                .price(bookingDto.getPrice())
                .build();

        Booking savedBooking = bookingRepository.save(booking);
        return DtoMapper.toDto(savedBooking);
    }

    public BookingDto updateBooking(int id, BookingDto bookingDto) {
        var principal = SecurityUtils.getCurrentPrincipal();
        if (principal.getRole() != AppRole.ROLE_ADMIN) {
            throw new AccessDeniedException("Access denied");
        }

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking with id=%d not found".formatted(id)));

        Accommodation accommodation = accommodationRepository.findById(bookingDto.getAccommodationId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Accommodation with id=%d not found".formatted(bookingDto.getAccommodationId())));

        User user = userRepository.findById(bookingDto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with id=%d not found".formatted(bookingDto.getUserId())));

        booking.setAccommodation(accommodation);
        booking.setUser(user);
        booking.setCheckIn(bookingDto.getCheckIn());
        booking.setCheckOut(bookingDto.getCheckOut());
        booking.setPrice(bookingDto.getPrice());

        Booking savedBooking = bookingRepository.save(booking);
        return DtoMapper.toDto(savedBooking);
    }

    public void deleteBooking(int id) {
        var principal = SecurityUtils.getCurrentPrincipal();
        if (principal.getRole() != AppRole.ROLE_ADMIN) {
            throw new AccessDeniedException("Access denied");
        }

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking with id=%d not found".formatted(id)));

        bookingRepository.delete(booking);
    }

    private PageResponse<BookingDto> buildBookingPageResponse(Page<Booking> bookings) {
        List<BookingDto> content = bookings.getContent().stream()
                .map(DtoMapper::toDto)
                .collect(Collectors.toList());

        PageResponse<BookingDto> response = new PageResponse<>();
        response.setContent(content);
        response.setPage(bookings.getNumber());
        response.setSize(bookings.getSize());
        response.setTotalElements(bookings.getTotalElements());
        response.setTotalPages(bookings.getTotalPages());
        response.setHasNext(bookings.hasNext());
        return response;
    }

    private PageResponse<BookingDto> buildBookingPageResponse(List<Booking> bookings, int page, int size) {
        int fromIndex = Math.min(page * size, bookings.size());
        int toIndex = Math.min(fromIndex + size, bookings.size());

        List<BookingDto> content = bookings.subList(fromIndex, toIndex).stream()
                .map(DtoMapper::toDto)
                .toList();

        PageResponse<BookingDto> response = new PageResponse<>();
        response.setContent(content);
        response.setPage(page);
        response.setSize(size);
        response.setTotalElements((long) bookings.size());
        response.setTotalPages((int) Math.ceil((double) bookings.size() / size));
        response.setHasNext(toIndex < bookings.size());
        return response;
    }

    private Sort buildSort(String sortBy, String sortDir) {
        return sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
    }

    private List<Booking> sortBookings(List<Booking> bookings, String sortBy, String sortDir) {
        Comparator<Booking> comparator = comparatorForBooking(sortBy);
        if (!sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())) {
            comparator = comparator.reversed();
        }

        return bookings.stream()
                .sorted(comparator)
                .toList();
    }

    private Comparator<Booking> comparatorForBooking(String sortBy) {
        return switch (sortBy) {
            case "accommodationId", "accommodation_id" ->
                Comparator.comparingInt(booking -> booking.getAccommodation().getId());
            case "userId", "user_id" -> Comparator.comparingInt(booking -> booking.getUser().getId());
            case "checkIn", "check_in" -> Comparator.comparing(Booking::getCheckIn);
            case "checkOut", "check_out" -> Comparator.comparing(Booking::getCheckOut);
            case "price" -> Comparator.comparingLong(Booking::getPrice);
            default -> Comparator.comparingInt(Booking::getId);
        };
    }
}