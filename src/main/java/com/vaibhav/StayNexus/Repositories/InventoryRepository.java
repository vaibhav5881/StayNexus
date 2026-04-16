package com.vaibhav.StayNexus.Repositories;

import com.vaibhav.StayNexus.Entities.HotelEntity;
import com.vaibhav.StayNexus.Entities.InventoryEntity;
import com.vaibhav.StayNexus.Entities.RoomEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<InventoryEntity , Long> {

    void deleteByRoom(RoomEntity room);

    List<InventoryEntity> findByHotelAndDateBetween(HotelEntity hotel ,
                                                    LocalDate startDate,
                                                    LocalDate endDate);

    List<InventoryEntity> findByRoomOrderByDate(RoomEntity room);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT i FROM InventoryEntity i
            WHERE i.room.id = :roomId
              AND i.date BETWEEN :startDate AND :endDate
              AND i.closed = false
              AND (i.totalCount - i.bookCount - i.reservedCount) >= :roomsCount
            """)
    List<InventoryEntity> findAndLockAvailableInventory(
            @Param("roomId") Long roomId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("roomsCount") Integer roomsCount
    );

    @Modifying
    @Query("""
            UPDATE InventoryEntity i
            SET i.reservedCount = i.reservedCount + :roomsCount
            WHERE i.room.id = :roomId
              AND i.date BETWEEN :startDate AND :endDate
              AND (i.totalCount - i.bookCount - i.reservedCount) >= :roomsCount
              AND i.closed = false
            """)
    void initBooking(
            @Param("roomId") Long roomId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("roomsCount") Integer roomsCount
    );


    @Modifying
    @Query("""
            UPDATE InventoryEntity i
            SET i.reservedCount = i.reservedCount - :numberOfRooms,
                i.bookCount = i.bookCount + :numberOfRooms
            WHERE i.room.id = :roomId
              AND i.date BETWEEN :startDate AND :endDate
              AND (i.totalCount - i.bookCount) >= :numberOfRooms
              AND i.reservedCount >= :numberOfRooms
              AND i.closed = false
            """)
    void confirmBooking(
            @Param("roomId") Long roomId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("numberOfRooms") int numberOfRooms
    );

    @Modifying
    @Query("""
            UPDATE InventoryEntity i
            SET i.bookCount = i.bookCount - :numberOfRooms
            WHERE i.room.id = :roomId
              AND i.date BETWEEN :startDate AND :endDate
              AND (i.totalCount - i.bookCount) >= :numberOfRooms
              AND i.closed = false
            """)
    void cancelBooking(
            @Param("roomId") Long roomId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("numberOfRooms") int numberOfRooms
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT i FROM InventoryEntity i
            WHERE i.room.id = :roomId
              AND i.date BETWEEN :startDate AND :endDate
            """)
    List<InventoryEntity> getInventoryAndLockBeforeUpdate(
            @Param("roomId") Long roomId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Modifying
    @Query("""
            UPDATE InventoryEntity i
            SET i.surgeFactor = :surgeFactor,
                i.closed = :closed
            WHERE i.room.id = :roomId
              AND i.date BETWEEN :startDate AND :endDate
            """)
    void updateInventory(
            @Param("roomId") Long roomId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("closed") boolean closed,
            @Param("surgeFactor") BigDecimal surgeFactor
    );

}





















