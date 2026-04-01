package ru.ifmo.se.lab.dto.payment;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "payment_method_name", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = YookassaPaymentDataDto.class, name = "yookassa")
})
public class PaymentDataDto {
    private int id;

    @JsonAlias("user_id")
    @Positive
    private int userId;

    @JsonAlias("payment_method_name")
    @NotBlank
    private String paymentMethodName;
}