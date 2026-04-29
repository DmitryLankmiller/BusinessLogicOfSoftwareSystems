package ru.ifmo.se.lab.service.mail;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import ru.ifmo.se.lab.model.Booking;
import ru.ifmo.se.lab.model.BookingRequest;

@Service
@Slf4j
public class ConsoleMailService implements MailService {

    @Override
    public void send(String to, String subject, String body) {
        log.info("Sending email to=%s subject=%s body=%s\n".formatted(to, subject, body));
    }

    @Override
    public void sendBookingRequestToHost(BookingRequest bookingRequest) {
        send(
                bookingRequest.getHost().getEmail(),
                "Booking request",
                "New booking request for accommodation %d from user %d".formatted(
                        bookingRequest.getAccommodation().getId(),
                        bookingRequest.getClient().getId()));
        // throw new RuntimeException("Test exception");
    }

    @Override
    public void sendBookingConfirmedToClient(Booking booking) {
        send(
                booking.getUser().getEmail(),
                "Booking confirmed",
                "Booking %d was confirmed for accommodation %d".formatted(
                        booking.getId(),
                        booking.getAccommodation().getId()));
    }

    @Override
    public void sendCaptureFailedToClient(int bookingId, String userEmail, int accommodationId) {
        send(
                userEmail,
                "Capture for booking failed",
                "Capture for booking %d for accommodation %d was failed".formatted(
                        bookingId,
                        accommodationId));
    }

    @Override
    public void sendBookingRejectedToClient(BookingRequest bookingRequest, String reason) {
        send(
                bookingRequest.getClient().getEmail(),
                "Booking rejected",
                "Booking request %d was rejected. %s".formatted(
                        bookingRequest.getId(),
                        reason == null ? "" : reason));
    }

    @Override
    public void sendBookingExpiredToClient(BookingRequest bookingRequest) {
        send(
                bookingRequest.getClient().getEmail(),
                "Booking expired",
                "Booking request %d expired because the host did not respond in time".formatted(
                        bookingRequest.getId()));
    }
}