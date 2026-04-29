package ru.ifmo.se.lab.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ru.ifmo.se.lab.model.PaymentData;

@Repository
public interface PaymentDataRepository extends JpaRepository<PaymentData, Integer> {
    List<PaymentData> findAllByUserId(long userId);
}