package ru.ifmo.se.lab.controller;

import java.time.LocalDate;

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
import ru.ifmo.se.lab.dto.AccommodationDto;
import ru.ifmo.se.lab.dto.PageResponse;
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
        return accommodationCrudService.findAccommodations(page, size, sortBy,
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
        return accommodationCrudService.findAvailableAccommodations(checkIn,
                checkOut, guestsCount, page, size, sortBy,
                sortDir);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HOST')")
    @GetMapping("/{id}")
    public AccommodationDto getAccommodationById(@PathVariable @Positive int id) {
        return accommodationCrudService.findAccommodationById(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HOST')")
    @PostMapping
    public AccommodationDto createAccommodation(@RequestBody @Valid AccommodationDto accommodationDto) {
        return accommodationCrudService.addAccommodation(accommodationDto);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HOST')")
    @PutMapping("/{id}")
    public AccommodationDto updateAccommodation(
            @PathVariable @Positive int id,
            @RequestBody @Valid AccommodationDto accommodationDto) {
        return accommodationCrudService.updateAccommodation(id, accommodationDto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void deleteAccommodation(@PathVariable @Positive int id) {
        accommodationCrudService.deleteAccommodation(id);
    }
}