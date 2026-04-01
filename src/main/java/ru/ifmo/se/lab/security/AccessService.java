package ru.ifmo.se.lab.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import ru.ifmo.se.lab.model.Accommodation;
import ru.ifmo.se.lab.model.Booking;
import ru.ifmo.se.lab.model.BookingRequest;

@Service
@RequiredArgsConstructor
public class AccessService {

    public void checkAccommodationReadAccess(AppPrincipal principal, Accommodation accommodation) {
        if (principal.getRole() == AppRole.ADMIN) {
            return;
        }

        if (principal.getRole() == AppRole.HOST && accommodation.getHost().getId() == principal.getId()) {
            return;
        }

        throw new AccessDeniedException("Access denied");
    }

    public void checkAccommodationWriteAccess(AppPrincipal principal, Accommodation accommodation) {
        checkAccommodationReadAccess(principal, accommodation);
    }

    public void checkAccommodationCreateAccess(AppPrincipal principal, int hostId) {
        if (principal.getRole() == AppRole.ADMIN) {
            return;
        }

        if (principal.getRole() == AppRole.HOST && principal.getId() == hostId) {
            return;
        }

        throw new AccessDeniedException("Access denied");
    }

    public void checkBookingReadAccess(AppPrincipal principal, Booking booking) {
        if (principal.getRole() == AppRole.ADMIN) {
            return;
        }

        if (principal.getRole() == AppRole.USER && booking.getUser().getId() == principal.getId()) {
            return;
        }

        if (principal.getRole() == AppRole.HOST && booking.getAccommodation().getHost().getId() == principal.getId()) {
            return;
        }

        throw new AccessDeniedException("Access denied");
    }

    public void checkBookingRequestReadAccess(AppPrincipal principal, BookingRequest bookingRequest) {
        if (principal.getRole() == AppRole.ADMIN) {
            return;
        }

        if (principal.getRole() == AppRole.USER && bookingRequest.getClient().getId() == principal.getId()) {
            return;
        }

        if (principal.getRole() == AppRole.HOST && bookingRequest.getHost().getId() == principal.getId()) {
            return;
        }

        throw new AccessDeniedException("Access denied");
    }

    public void checkBookingRequestResolveAccess(AppPrincipal principal, BookingRequest bookingRequest) {
        if (principal.getRole() == AppRole.ADMIN) {
            return;
        }

        if (principal.getRole() == AppRole.HOST && bookingRequest.getHost().getId() == principal.getId()) {
            return;
        }

        throw new AccessDeniedException("Access denied");
    }
}