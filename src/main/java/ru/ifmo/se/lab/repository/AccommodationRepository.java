package ru.ifmo.se.lab.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ru.ifmo.se.lab.model.Accommodation;

@Repository
public interface AccommodationRepository extends JpaRepository<Accommodation, Integer> {
    List<Accommodation> findAllByHostId(int hostId);

    @Query("SELECT a FROM Accommodation a LEFT JOIN FETCH a.host WHERE a.host.id = :hostId")
    List<Accommodation> findAllByHostIdWithHost(@Param("hostId") int hostId);

    @Query("SELECT a FROM Accommodation a LEFT JOIN FETCH a.host")
    Page<Accommodation> findAllWithHost(Pageable pageable);
}