package ru.ifmo.se.lab.dto.payment;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonTypeName;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonTypeName("yookassa")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class YookassaInputInfo implements PaymentMethodInputInfo {
    @NotBlank
    @Pattern(regexp = "\\d{3,4}")
    private String csc;

    @JsonAlias("expiry_month")
    @NotBlank
    @Pattern(regexp = "^(0[1-9]|1[0-2])$")
    private String expiryMonth;

    @JsonAlias("expiry_year")
    @NotBlank
    @Pattern(regexp = "^\\d{2,4}$")
    private String expiryYear;

    @NotBlank
    @Pattern(regexp = "^\\d{16}$")
    private String number;
}