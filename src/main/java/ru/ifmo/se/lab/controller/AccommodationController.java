package ru.ifmo.se.lab.controller;

import java.time.LocalDate;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import ru.ifmo.se.lab.dto.AccommodationDto;
import ru.ifmo.se.lab.dto.PageResponse;
import ru.ifmo.se.lab.security.SecurityUtils;
import ru.ifmo.se.lab.service.AccommodationCrudService;

@RestController
@RequestMapping("/accommodations")
@RequiredArgsConstructor
@Validated
public class AccommodationController {

    private final AccommodationCrudService accommodationCrudService;

    @PreAuthorize("hasAnyRole('ADMIN', 'HOST')")
    @GetMapping(params = "!check_in")
    public PageResponse<AccommodationDto> getAccommodations(
            @RequestParam(value = "page", defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(value = "size", defaultValue = "10") @Positive int size,
            @RequestParam(value = "sort_by", defaultValue = "id") String sortBy,
            @RequestParam(value = "sort_dir", defaultValue = "asc") String sortDir) {
        return accommodationCrudService.findAccommodations(SecurityUtils.getCurrentPrincipal(), page, size, sortBy,
                sortDir);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping(params = { "check_in", "check_out", "guests_count" })
    public PageResponse<AccommodationDto> searchAvailableAccommodations(
            @RequestParam("check_in") LocalDate checkIn,
            @RequestParam("check_out") LocalDate checkOut,
            @RequestParam("guests_count") @Positive short guestsCount,
            @RequestParam(value = "page", defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(value = "size", defaultValue = "10") @Positive int size,
            @RequestParam(value = "sort_by", defaultValue = "id") String sortBy,
            @RequestParam(value = "sort_dir", defaultValue = "asc") String sortDir) {
        return accommodationCrudService.findAvailableAccommodations(SecurityUtils.getCurrentPrincipal(), checkIn,
                checkOut, guestsCount, page, size, sortBy,
                sortDir);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HOST')")
    @GetMapping("/{id}")
    public AccommodationDto getAccommodationById(@PathVariable @Positive int id) {
        return accommodationCrudService.findAccommodationById(SecurityUtils.getCurrentPrincipal(), id);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HOST')")
    @PostMapping
    public AccommodationDto createAccommodation(@RequestBody @Valid AccommodationDto accommodationDto) {
        return accommodationCrudService.addAccommodation(SecurityUtils.getCurrentPrincipal(), accommodationDto);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HOST')")
    @PutMapping("/{id}")
    public AccommodationDto updateAccommodation(
            @PathVariable @Positive int id,
            @RequestBody @Valid AccommodationDto accommodationDto) {
        return accommodationCrudService.updateAccommodation(SecurityUtils.getCurrentPrincipal(), id, accommodationDto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void deleteAccommodation(@PathVariable @Positive int id) {
        accommodationCrudService.deleteAccommodation(SecurityUtils.getCurrentPrincipal(), id);
    }
}