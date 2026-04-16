package com.vaibhav.StayNexus.Repositories;

import com.vaibhav.StayNexus.Entities.HotelEntity;
import com.vaibhav.StayNexus.Entities.HotelMinPriceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface HotelMinPriceRepository extends JpaRepository<HotelMinPriceEntity, Long> {

    @Query("""
            SELECT DISTINCT h FROM HotelMinPriceEntity h
            JOIN InventoryEntity i ON i.hotel = h.hotel AND i.date = h.date
            WHERE h.hotel.city = :city
              AND h.date BETWEEN :startDate AND :endDate
              AND i.closed = false
              AND (i.totalCount - i.bookCount - i.reservedCount) >= :roomsCount
            GROUP BY h.hotel, h.date
            HAVING COUNT(h.date) = :dateCount
            """)
    Page<HotelMinPriceEntity> findHotelsWithAvailableInventory(
            @Param("city") String city,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("roomsCount") Integer roomsCount,
            @Param("dateCount") Long dateCount,
            Pageable pageable
    );

    Optional<HotelMinPriceEntity> findByHotelAndDate(HotelEntity hotel , LocalDate date);
}
