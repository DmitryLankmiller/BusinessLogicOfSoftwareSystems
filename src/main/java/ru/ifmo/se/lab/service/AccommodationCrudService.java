package ru.ifmo.se.lab.service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import ru.ifmo.se.lab.dto.AccommodationDto;
import ru.ifmo.se.lab.dto.DtoMapper;
import ru.ifmo.se.lab.dto.PageResponse;
import ru.ifmo.se.lab.exception.ResourceNotFoundException;
import ru.ifmo.se.lab.model.Accommodation;
import ru.ifmo.se.lab.model.User;
import ru.ifmo.se.lab.repository.AccommodationRepository;
import ru.ifmo.se.lab.repository.BookingRepository;
import ru.ifmo.se.lab.repository.UserRepository;
import ru.ifmo.se.lab.security.AccessService;
import ru.ifmo.se.lab.security.AppPrincipal;
import ru.ifmo.se.lab.security.AppRole;

@Service
@RequiredArgsConstructor
public class AccommodationCrudService {

    private final AccommodationRepository accommodationRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final AccessService accessService;

    public PageResponse<AccommodationDto> findAccommodations(
            AppPrincipal principal,
            int page,
            int size,
            String sortBy,
            String sortDir) {
        if (principal.getRole() == AppRole.ADMIN) {
            Sort sort = buildSort(sortBy, sortDir);
            Page<Accommodation> accommodations = accommodationRepository.findAll(PageRequest.of(page, size, sort));
            return buildAccommodationPageResponse(accommodations);
        }

        if (principal.getRole() == AppRole.HOST) {
            List<Accommodation> accommodations = accommodationRepository.findAllByHostId(principal.getId());
            List<Accommodation> sorted = sortAccommodations(accommodations, sortBy, sortDir);
            return buildAccommodationPageResponse(sorted, page, size);
        }

        throw new AccessDeniedException("Access denied");
    }

    public PageResponse<AccommodationDto> findAvailableAccommodations(
            AppPrincipal principal,
            LocalDate checkIn,
            LocalDate checkOut,
            short guestsCount,
            int page,
            int size,
            String sortBy,
            String sortDir) {
        if (principal.getRole() != AppRole.ADMIN && principal.getRole() != AppRole.USER) {
            throw new AccessDeniedException("Access denied");
        }

        List<Accommodation> available = accommodationRepository.findAll().stream()
                .filter(Accommodation::isPublished)
                .filter(accommodation -> accommodation.getMaxGuestsNumber() >= guestsCount)
                .filter(accommodation -> bookingRepository
                        .findAllByAccommodationIdAndCheckInLessThanAndCheckOutGreaterThan(
                                accommodation.getId(),
                                checkOut,
                                checkIn)
                        .isEmpty())
                .toList();

        List<Accommodation> sorted = sortAccommodations(available, sortBy, sortDir);
        return buildAccommodationPageResponse(sorted, page, size);
    }

    public AccommodationDto findAccommodationById(AppPrincipal principal, int id) {
        Accommodation accommodation = accommodationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Accommodation with id=%d not found".formatted(id)));

        accessService.checkAccommodationReadAccess(principal, accommodation);
        return DtoMapper.toDto(accommodation);
    }

    public AccommodationDto addAccommodation(AppPrincipal principal, AccommodationDto accommodationDto) {
        accessService.checkAccommodationCreateAccess(principal, accommodationDto.getHostId());

        User host = userRepository.findById(accommodationDto.getHostId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with id=%d not found".formatted(accommodationDto.getHostId())));

        Accommodation accommodation = Accommodation.builder()
                .host(host)
                .name(accommodationDto.getName())
                .description(accommodationDto.getDescription())
                .maxGuestsNumber(accommodationDto.getMaxGuestsNumber())
                .bedsCount(accommodationDto.getBedsCount())
                .address(accommodationDto.getAddress())
                .rating(accommodationDto.getRating() == null ? 0f : accommodationDto.getRating())
                .pricePerNight(accommodationDto.getPricePerNight())
                .isPublished(Boolean.TRUE.equals(accommodationDto.getPublished())
                        && principal.getRole().equals(AppRole.ADMIN))
                .build();

        Accommodation savedAccommodation = accommodationRepository.save(accommodation);
        return DtoMapper.toDto(savedAccommodation);
    }

    public AccommodationDto updateAccommodation(AppPrincipal principal, int id, AccommodationDto accommodationDto) {
        Accommodation accommodation = accommodationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Accommodation with id=%d not found".formatted(id)));

        accessService.checkAccommodationWriteAccess(principal, accommodation);

        User host = userRepository.findById(accommodationDto.getHostId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with id=%d not found".formatted(accommodationDto.getHostId())));

        if (principal.getRole() == AppRole.HOST && principal.getId() != host.getId()) {
            throw new AccessDeniedException("Access denied");
        }

        accommodation.setHost(host);
        accommodation.setName(accommodationDto.getName());
        accommodation.setDescription(accommodationDto.getDescription());
        accommodation.setMaxGuestsNumber(accommodationDto.getMaxGuestsNumber());
        accommodation.setBedsCount(accommodationDto.getBedsCount());
        accommodation.setAddress(accommodationDto.getAddress());
        accommodation.setRating(accommodationDto.getRating() == null ? 0f : accommodationDto.getRating());
        accommodation.setPricePerNight(accommodationDto.getPricePerNight());
        accommodation.setPublished(
                Boolean.TRUE.equals(accommodationDto.getPublished()) && principal.getRole().equals(AppRole.ADMIN));

        Accommodation savedAccommodation = accommodationRepository.save(accommodation);
        return DtoMapper.toDto(savedAccommodation);
    }

    public void deleteAccommodation(AppPrincipal principal, int id) {
        if (principal.getRole() != AppRole.ADMIN) {
            throw new AccessDeniedException("Access denied");
        }

        Accommodation accommodation = accommodationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Accommodation with id=%d not found".formatted(id)));

        accommodationRepository.delete(accommodation);
    }

    private PageResponse<AccommodationDto> buildAccommodationPageResponse(Page<Accommodation> accommodations) {
        List<AccommodationDto> content = accommodations.getContent().stream()
                .map(DtoMapper::toDto)
                .collect(Collectors.toList());

        PageResponse<AccommodationDto> response = new PageResponse<>();
        response.setContent(content);
        response.setPage(accommodations.getNumber());
        response.setSize(accommodations.getSize());
        response.setTotalElements(accommodations.getTotalElements());
        response.setTotalPages(accommodations.getTotalPages());
        response.setHasNext(accommodations.hasNext());
        return response;
    }

    private PageResponse<AccommodationDto> buildAccommodationPageResponse(
            List<Accommodation> accommodations,
            int page,
            int size) {
        int fromIndex = Math.min(page * size, accommodations.size());
        int toIndex = Math.min(fromIndex + size, accommodations.size());

        List<AccommodationDto> content = accommodations.subList(fromIndex, toIndex).stream()
                .map(DtoMapper::toDto)
                .toList();

        PageResponse<AccommodationDto> response = new PageResponse<>();
        response.setContent(content);
        response.setPage(page);
        response.setSize(size);
        response.setTotalElements((long) accommodations.size());
        response.setTotalPages((int) Math.ceil((double) accommodations.size() / size));
        response.setHasNext(toIndex < accommodations.size());
        return response;
    }

    private Sort buildSort(String sortBy, String sortDir) {
        return sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
    }

    private List<Accommodation> sortAccommodations(List<Accommodation> accommodations, String sortBy, String sortDir) {
        Comparator<Accommodation> comparator = comparatorForAccommodation(sortBy);
        if (!sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())) {
            comparator = comparator.reversed();
        }

        return accommodations.stream()
                .sorted(comparator)
                .toList();
    }

    private Comparator<Accommodation> comparatorForAccommodation(String sortBy) {
        return switch (sortBy) {
            case "hostId", "host_id" -> Comparator.comparingInt(accommodation -> accommodation.getHost().getId());
            case "name" -> Comparator.comparing(Accommodation::getName, String.CASE_INSENSITIVE_ORDER);
            case "description" -> Comparator.comparing(Accommodation::getDescription, String.CASE_INSENSITIVE_ORDER);
            case "maxGuestsNumber", "max_guests_number" -> Comparator.comparingInt(Accommodation::getMaxGuestsNumber);
            case "bedsCount", "beds_count" -> Comparator.comparingInt(Accommodation::getBedsCount);
            case "address" -> Comparator.comparing(Accommodation::getAddress, String.CASE_INSENSITIVE_ORDER);
            case "rating" -> Comparator.comparingDouble(Accommodation::getRating);
            case "pricePerNight", "price_per_night" -> Comparator.comparingLong(Accommodation::getPricePerNight);
            case "published", "is_published" -> Comparator.comparing(Accommodation::isPublished);
            default -> Comparator.comparingInt(Accommodation::getId);
        };
    }
}