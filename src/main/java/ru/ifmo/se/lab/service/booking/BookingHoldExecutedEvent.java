package ru.ifmo.se.lab.service.booking;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;
import ru.ifmo.se.lab.model.PaymentData;

@Getter
public class BookingHoldExecutedEvent extends ApplicationEvent {

    private final PaymentData paymentData;

    public BookingHoldExecutedEvent(Object source, PaymentData paymentData) {
        super(source);
        this.paymentData = paymentData;
    }

}
