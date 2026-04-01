package ru.ifmo.se.lab.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ru.ifmo.se.lab.model.Booking;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {
    List<Booking> findAllByAccommodationId(int accommodationId);

    List<Booking> findAllByAccommodationIdAndCheckInLessThanAndCheckOutGreaterThan(
            int accommodationId,
            LocalDate checkOut,
            LocalDate checkIn);

    List<Booking> findAllByUserId(int userId);

    List<Booking> findAllByAccommodationHostId(int hostId);

    List<Booking> findAllByAccommodationIdAndAccommodationHostId(int accommodationId, int hostId);
}