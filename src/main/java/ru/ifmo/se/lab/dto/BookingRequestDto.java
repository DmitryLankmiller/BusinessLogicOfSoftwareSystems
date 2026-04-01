package ru.ifmo.se.lab.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonAlias;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingRequestDto {
    private Integer id;

    @JsonAlias("accommodation_id")
    @Positive
    private Integer accommodationId;

    @JsonAlias("client_id")
    @Positive
    private Integer clientId;

    @JsonAlias("host_id")
    @Positive
    private Integer hostId;

    @JsonAlias("payment_data_id")
    @Positive
    private Integer paymentDataId;

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
}