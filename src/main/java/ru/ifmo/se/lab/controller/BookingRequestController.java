package ru.ifmo.se.lab.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import ru.ifmo.se.lab.dto.BookingRequestCreateDto;
import ru.ifmo.se.lab.dto.BookingRequestDto;
import ru.ifmo.se.lab.dto.BookingRequestResolutionDto;
import ru.ifmo.se.lab.dto.PageResponse;
import ru.ifmo.se.lab.security.SecurityUtils;
import ru.ifmo.se.lab.service.BookingOrchestrationService;
import ru.ifmo.se.lab.service.BookingRequestCrudService;

@RestController
@RequestMapping("/booking-requests")
@RequiredArgsConstructor
@Validated
public class BookingRequestController {

    private final BookingRequestCrudService bookingRequestCrudService;
    private final BookingOrchestrationService bookingOrchestrationService;

    @GetMapping(params = "!accommodation")
    public PageResponse<BookingRequestDto> getBookingRequests(
            @RequestParam(value = "page", defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(value = "size", defaultValue = "10") @Positive int size,
            @RequestParam(value = "sort_by", defaultValue = "id") String sortBy,
            @RequestParam(value = "sort_dir", defaultValue = "asc") String sortDir) {
        return bookingRequestCrudService.findBookingRequests(SecurityUtils.getCurrentPrincipal(), page, size, sortBy,
                sortDir);
    }

    @GetMapping(params = "accommodation")
    public PageResponse<BookingRequestDto> getBookingRequestsByAccommodation(
            @RequestParam("accommodation") @Positive int accommodationId,
            @RequestParam(value = "page", defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(value = "size", defaultValue = "10") @Positive int size,
            @RequestParam(value = "sort_by", defaultValue = "id") String sortBy,
            @RequestParam(value = "sort_dir", defaultValue = "asc") String sortDir) {
        return bookingRequestCrudService.findBookingRequestsByAccommodation(SecurityUtils.getCurrentPrincipal(),
                accommodationId, page, size, sortBy,
                sortDir);
    }

    @GetMapping("/{id}")
    public BookingRequestDto getBookingRequestById(@PathVariable @Positive int id) {
        return bookingRequestCrudService.findBookingRequestById(SecurityUtils.getCurrentPrincipal(), id);
    }

    @PostMapping
    public BookingRequestDto createBookingRequest(@RequestBody @Valid BookingRequestCreateDto bookingRequestCreateDto) {
        return bookingOrchestrationService.createBookingRequest(SecurityUtils.getCurrentPrincipal(),
                bookingRequestCreateDto);
    }

    @PutMapping("/{id}")
    public BookingRequestDto updateBookingRequest(
            @PathVariable @Positive int id,
            @RequestBody @Valid BookingRequestDto bookingRequestDto) {
        return bookingRequestCrudService.updateBookingRequest(SecurityUtils.getCurrentPrincipal(), id,
                bookingRequestDto);
    }

    @DeleteMapping("/{id}")
    public void deleteBookingRequest(@PathVariable @Positive int id) {
        bookingRequestCrudService.deleteBookingRequest(SecurityUtils.getCurrentPrincipal(), id);
    }

    @PostMapping("/{id}/resolved")
    public ResponseEntity<Void> resolveBookingRequest(
            @PathVariable @Positive int id,
            @RequestBody @Valid BookingRequestResolutionDto requestResolutionDto) {
        bookingOrchestrationService.resolveBookingRequest(SecurityUtils.getCurrentPrincipal(),
                id,
                requestResolutionDto.getConfirm(),
                requestResolutionDto.getReason());
        return ResponseEntity.ok().build();
    }
}