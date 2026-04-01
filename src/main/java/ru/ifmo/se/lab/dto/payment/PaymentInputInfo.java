package ru.ifmo.se.lab.dto.payment;

import com.fasterxml.jackson.annotation.JsonAlias;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentInputInfo {
    @JsonAlias("payment_method")
    @NotBlank
    private String paymentMethod;

    @JsonAlias("payment_method_input_info")
    @NotNull
    @Valid
    private PaymentMethodInputInfo paymentMethodInputInfo;
}