package ru.ifmo.se.lab.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "booking_request")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "accommodation_id")
    @NotNull
    private Accommodation accommodation;

    @ManyToOne
    @JoinColumn(name = "client_id")
    @NotNull
    private User client;

    @ManyToOne
    @JoinColumn(name = "host_id")
    @NotNull
    private User host;

    @OneToOne
    @JoinColumn(name = "payment_data_id")
    @NotNull
    private PaymentData paymentData;

    @Column(name = "check_in")
    @NotNull
    private LocalDate checkIn;

    @Column(name = "check_out")
    @NotNull
    private LocalDate checkOut;

    @Column(name = "message_to_host")
    @NotEmpty
    private String messageToHost;
}
