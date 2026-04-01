package ru.ifmo.se.lab.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonAlias;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingDto {
    private Integer id;

    @JsonAlias("accommodation_id")
    @Positive
    private Integer accommodationId;

    @JsonAlias("user_id")
    @Positive
    private Integer userId;

    @JsonAlias("check_in")
    @NotNull
    @FutureOrPresent
    private LocalDate checkIn;

    @JsonAlias("check_out")
    @NotNull
    @FutureOrPresent
    private LocalDate checkOut;

    @Positive
    private Long price;
}