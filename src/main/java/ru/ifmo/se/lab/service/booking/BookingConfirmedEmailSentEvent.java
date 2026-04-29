package ru.ifmo.se.lab.service.booking;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;

@Getter
public class BookingConfirmedEmailSentEvent extends ApplicationEvent {

    private final int bookingId;
    private final String userEmail;
    private final int accommodationId;

    public BookingConfirmedEmailSentEvent(Object source, int bookingId, String userEmail, int accommodationId) {
        super(source);
        this.bookingId = bookingId;
        this.userEmail = userEmail;
        this.accommodationId = accommodationId;
    }

}
