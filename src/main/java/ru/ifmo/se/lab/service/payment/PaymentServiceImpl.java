package ru.ifmo.se.lab.service.payment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import ru.ifmo.se.lab.dto.payment.PaymentInputInfo;
import ru.ifmo.se.lab.dto.payment.PaymentMethodInputInfo;
import ru.ifmo.se.lab.model.PaymentData;
import ru.ifmo.se.lab.model.User;
import ru.ifmo.se.lab.repository.PaymentDataRepository;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentDataRepository paymentDataRepository;
    private final List<PaymentProcessor<?, ?>> paymentProcessors;

    private final Map<String, PaymentProcessor<?, ?>> paymentProcessorByMethodName = new HashMap<>();
    private final Map<Class<? extends PaymentData>, PaymentProcessor<?, ?>> paymentProcessorByPaymentDataType = new HashMap<>();

    @PostConstruct
    public void init() {
        for (PaymentProcessor<?, ?> paymentProcessor : paymentProcessors) {
            paymentProcessorByMethodName.put(paymentProcessor.paymentMethodName(), paymentProcessor);
            paymentProcessorByPaymentDataType.put(paymentProcessor.paymentDataType(), paymentProcessor);
        }
    }

    @Override
    public PaymentData executeHold(User user, long amount, String description, PaymentInputInfo paymentInputInfo) {
        PaymentProcessor<?, ?> paymentProcessor = paymentProcessorByMethodName.get(paymentInputInfo.getPaymentMethod());
        if (paymentProcessor == null) {
            throw new IllegalArgumentException("Unsupported payment method: " + paymentInputInfo.getPaymentMethod());
        }

        PaymentData paymentData = executeHold(paymentProcessor, user, amount, description, paymentInputInfo);
        return paymentDataRepository.save(paymentData);
    }

    @Override
    public void handleCapture(PaymentData payment) {
        PaymentProcessor<?, ?> paymentProcessor = findProcessorByPayment(payment);
        capture(paymentProcessor, payment);
    }

    @Override
    public void handleCancel(PaymentData payment) {
        PaymentProcessor<?, ?> paymentProcessor = findProcessorByPayment(payment);
        cancel(paymentProcessor, payment);
    }

    @SuppressWarnings("unchecked")
    private PaymentData executeHold(
            PaymentProcessor<?, ?> paymentProcessor,
            User user,
            long amount,
            String description,
            PaymentInputInfo paymentInputInfo) {
        return ((PaymentProcessor<PaymentData, PaymentMethodInputInfo>) paymentProcessor).hold(
                user,
                amount,
                description,
                paymentInputInfo.getPaymentMethodInputInfo());
    }

    @SuppressWarnings("unchecked")
    private void capture(PaymentProcessor<?, ?> paymentProcessor, PaymentData payment) {
        ((PaymentProcessor<PaymentData, PaymentMethodInputInfo>) paymentProcessor).capture(payment);
    }

    @SuppressWarnings("unchecked")
    private void cancel(PaymentProcessor<?, ?> paymentProcessor, PaymentData payment) {
        ((PaymentProcessor<PaymentData, PaymentMethodInputInfo>) paymentProcessor).cancel(payment);
    }

    private PaymentProcessor<?, ?> findProcessorByPayment(PaymentData payment) {
        PaymentProcessor<?, ?> paymentProcessor = paymentProcessorByPaymentDataType.get(payment.getClass());
        if (paymentProcessor == null) {
            throw new IllegalArgumentException("Unsupported payment data type: " + payment.getClass().getName());
        }
        return paymentProcessor;
    }
}