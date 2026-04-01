package ru.ifmo.se.lab.service.payment;

import ru.ifmo.se.lab.dto.payment.PaymentMethodInputInfo;
import ru.ifmo.se.lab.model.PaymentData;
import ru.ifmo.se.lab.model.User;

public interface PaymentProcessor<P extends PaymentData, I extends PaymentMethodInputInfo> {
    String paymentMethodName();

    Class<P> paymentDataType();

    P hold(User user, long amount, String description, I paymentMethodInputInfo);

    void capture(P payment);

    void cancel(P payment);
}