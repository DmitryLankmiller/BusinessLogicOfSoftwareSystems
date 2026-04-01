package ru.ifmo.se.lab.service.payment;

import ru.ifmo.se.lab.dto.payment.PaymentInputInfo;
import ru.ifmo.se.lab.model.PaymentData;
import ru.ifmo.se.lab.model.User;

public interface PaymentService {
    PaymentData executeHold(User user, long amount, String description, PaymentInputInfo paymentInputInfo);

    void handleCapture(PaymentData payment);

    void handleCancel(PaymentData payment);
}