package ru.ifmo.se.lab.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonAlias;

import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.ifmo.se.lab.dto.payment.PaymentInputInfo;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingRequestCreateDto {
    @JsonAlias("accommodation_id")
    @Positive
    private Integer accommodationId;

    @JsonAlias("client_id")
    @Positive
    private Integer clientId;

    @JsonAlias("check_in")
    @NotNull
    @FutureOrPresent
    private LocalDate checkIn;

    @JsonAlias("check_out")
    @NotNull
    @FutureOrPresent
    private LocalDate checkOut;

    @JsonAlias("message_to_host")
    @NotBlank
    private String messageToHost;

    @JsonAlias("payment_input_info")
    @NotNull
    @Valid
    private PaymentInputInfo paymentInputInfo;
}