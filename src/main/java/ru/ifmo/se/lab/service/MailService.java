package ru.ifmo.se.lab.service;

import ru.ifmo.se.lab.model.Booking;
import ru.ifmo.se.lab.model.BookingRequest;

public interface MailService {
    void send(String to, String subject, String body);

    void sendBookingRequestToHost(BookingRequest bookingRequest);

    void sendBookingConfirmedToClient(Booking booking);

    void sendBookingRejectedToClient(BookingRequest bookingRequest, String reason);

    void sendBookingExpiredToClient(BookingRequest bookingRequest);
}