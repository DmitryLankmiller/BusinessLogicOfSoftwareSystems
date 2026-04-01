package ru.ifmo.se.lab.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ru.ifmo.se.lab.model.PaymentConfirmation;

@Repository
public interface PaymentConfirmationRepository extends JpaRepository<PaymentConfirmation, Integer> {

}
