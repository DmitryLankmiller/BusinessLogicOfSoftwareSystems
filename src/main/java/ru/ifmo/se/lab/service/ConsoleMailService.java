package ru.ifmo.se.lab.service;

import org.springframework.stereotype.Service;

import ru.ifmo.se.lab.model.Booking;
import ru.ifmo.se.lab.model.BookingRequest;

@Service
public class ConsoleMailService implements MailService {

    @Override
    public void send(String to, String subject, String body) {
        System.out.printf("Sending email to=%s subject=%s body=%s%n", to, subject, body);
    }

    @Override
    public void sendBookingRequestToHost(BookingRequest bookingRequest) {
        send(
                bookingRequest.getHost().getEmail(),
                "Booking request",
                "New booking request for accommodation %d from user %d".formatted(
                        bookingRequest.getAccommodation().getId(),
                        bookingRequest.getClient().getId()));
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