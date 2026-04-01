package ru.ifmo.se.lab.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "accommodation")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Accommodation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "host_id")
    @NotNull
    private User host;

    @Column(name = "name")
    @NotEmpty
    private String name;

    @Column(name = "description")
    @NotEmpty
    private String description;

    @Column(name = "max_guests_number")
    @NotNull
    @Positive
    private short maxGuestsNumber;

    @Column(name = "beds_count")
    @NotNull
    @PositiveOrZero
    private short bedsCount;

    @Column(name = "address")
    @NotEmpty
    private String address;

    @Column(name = "rating")
    @PositiveOrZero
    private float rating;

    @Column(name = "price_per_night")
    @NotNull
    @Positive
    private long pricePerNight;

    @Column(name = "is_published")
    @NotNull
    private boolean isPublished;
}
