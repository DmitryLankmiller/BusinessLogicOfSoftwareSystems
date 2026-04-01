package ru.ifmo.se.lab.service.payment;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.ifmo.se.lab.dto.payment.YookassaInputInfo;
import ru.ifmo.se.lab.model.User;
import ru.ifmo.se.lab.model.YookassaPaymentData;
import ru.ifmo.se.lab.service.payment.YookassaApiClient.Amount;
import ru.ifmo.se.lab.service.payment.YookassaApiClient.Card;
import ru.ifmo.se.lab.service.payment.YookassaApiClient.Confirmation;
import ru.ifmo.se.lab.service.payment.YookassaApiClient.PaymentMethodData;
import ru.ifmo.se.lab.service.payment.YookassaApiClient.YookassaCreatePaymentRequest;
import ru.ifmo.se.lab.service.payment.YookassaApiClient.YookassaCreatePaymentResponse;

@Component
@RequiredArgsConstructor
@Slf4j
public class YookassaPaymentProcessor implements PaymentProcessor<YookassaPaymentData, YookassaInputInfo> {

    private final YookassaApiClient yookassaApiClient;

    @Override
    public String paymentMethodName() {
        return "yookassa";
    }

    @Override
    public Class<YookassaPaymentData> paymentDataType() {
        return YookassaPaymentData.class;
    }

    @Override
    public YookassaPaymentData hold(
            User user,
            long amount,
            String description,
            YookassaInputInfo paymentMethodInputInfo) {
        log.info("============================================");
        log.info("YOOOOOOOOOKASSSSSAAAAAAAAAA");
        YookassaCreatePaymentRequest request = new YookassaCreatePaymentRequest(
                new Amount(formatAmount(amount), "RUB"),
                false,
                new PaymentMethodData(
                        "bank_card",
                        new Card(
                                paymentMethodInputInfo.getCsc(),
                                paymentMethodInputInfo.getExpiryMonth(),
                                paymentMethodInputInfo.getExpiryYear(),
                                paymentMethodInputInfo.getNumber())),
                new Confirmation("redirect", yookassaApiClient.getReturnUrl()),
                description);

        log.info(request.toString());
        YookassaCreatePaymentResponse response = yookassaApiClient.createHoldPayment(request);
        log.info(response.toString());
        log.info("============================================");
        return YookassaPaymentData.builder()
                .user(user)
                .paymentMethodName(paymentMethodName())
                .yookassaPaymentId(response.id())
                .build();
    }

    @Override
    public void capture(YookassaPaymentData payment) {
        yookassaApiClient.capturePayment(payment.getYookassaPaymentId());
    }

    @Override
    public void cancel(YookassaPaymentData payment) {
        yookassaApiClient.cancelPayment(payment.getYookassaPaymentId());
    }

    private String formatAmount(long amountInKopecks) {
        return BigDecimal.valueOf(amountInKopecks)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.UNNECESSARY)
                .toPlainString();
    }
}