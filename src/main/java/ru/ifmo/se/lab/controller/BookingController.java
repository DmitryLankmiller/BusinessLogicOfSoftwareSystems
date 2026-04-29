package ru.ifmo.se.lab.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import ru.ifmo.se.lab.dto.BookingDto;
import ru.ifmo.se.lab.dto.PageResponse;
import ru.ifmo.se.lab.service.booking.BookingCrudService;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Validated
public class BookingController {

    private final BookingCrudService bookingCrudService;

    @GetMapping(params = "!accommodation")
    public PageResponse<BookingDto> getBookings(
            @RequestParam(value = "page", defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(value = "size", defaultValue = "10") @Positive int size,
            @RequestParam(value = "sort_by", defaultValue = "id") String sortBy,
            @RequestParam(value = "sort_dir", defaultValue = "asc") String sortDir) {
        return bookingCrudService.findBookings(page, size, sortBy, sortDir);
    }

    @GetMapping(params = "accommodation")
    public PageResponse<BookingDto> getBookingsByAccommodation(
            @RequestParam("accommodation") @Positive int accommodationId,
            @RequestParam(value = "page", defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(value = "size", defaultValue = "10") @Positive int size,
            @RequestParam(value = "sort_by", defaultValue = "id") String sortBy,
            @RequestParam(value = "sort_dir", defaultValue = "asc") String sortDir) {
        return bookingCrudService.findBookingsByAccommodation(accommodationId,
                page, size, sortBy, sortDir);
    }

    @GetMapping("/{id}")
    public BookingDto getBookingById(@PathVariable @Positive int id) {
        return bookingCrudService.findBookingById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public BookingDto createBooking(@RequestBody @Valid BookingDto bookingDto) {
        return bookingCrudService.addBooking(bookingDto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public BookingDto updateBooking(
            @PathVariable @Positive int id,
            @RequestBody @Valid BookingDto bookingDto) {
        return bookingCrudService.updateBooking(id, bookingDto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void deleteBooking(@PathVariable @Positive int id) {
        bookingCrudService.deleteBooking(id);
    }
}