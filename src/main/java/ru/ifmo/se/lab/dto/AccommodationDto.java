package ru.ifmo.se.lab.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccommodationDto {
    private Integer id;

    @JsonAlias("host_id")
    @Positive
    private Integer hostId;

    @NotBlank
    private String name;

    @NotBlank
    private String description;

    @JsonAlias("max_guests_number")
    @Positive
    private Short maxGuestsNumber;

    @JsonAlias("beds_count")
    @PositiveOrZero
    private Short bedsCount;

    @NotBlank
    private String address;

    @DecimalMin(value = "0.0")
    @DecimalMax(value = "5.0")
    private Float rating;

    @JsonAlias("price_per_night")
    @Positive
    private Long pricePerNight;

    private Boolean published;
}