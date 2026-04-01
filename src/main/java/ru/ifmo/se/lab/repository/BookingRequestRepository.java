package ru.ifmo.se.lab.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ru.ifmo.se.lab.model.BookingRequest;

@Repository
public interface BookingRequestRepository extends JpaRepository<BookingRequest, Integer> {
    List<BookingRequest> findAllByAccommodationId(int accommodationId);

    List<BookingRequest> findAllByClientId(int clientId);

    List<BookingRequest> findAllByHostId(int hostId);

    List<BookingRequest> findAllByAccommodationIdAndHostId(int accommodationId, int hostId);
}