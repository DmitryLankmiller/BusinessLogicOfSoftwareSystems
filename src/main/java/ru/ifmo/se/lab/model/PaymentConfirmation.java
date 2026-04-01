package ru.ifmo.se.lab.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payment_confirmation")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentConfirmation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToOne
    @JoinColumn(name = "payment_data_id")
    @NotNull
    private PaymentData paymentData;

    @ManyToOne
    @JoinColumn(name = "booking_id")
    @NotNull
    private Booking booking;
}
