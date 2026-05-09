package com.vaibhav.StayNexus.Repositories;

import com.vaibhav.StayNexus.Entities.BookingEntity;
import com.vaibhav.StayNexus.Entities.HotelEntity;
import com.vaibhav.StayNexus.Entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<BookingEntity , Long> {

    Optional<BookingEntity> findByPaymentSessionId(String paymentSessionId);

    List<BookingEntity> findByUser(UserEntity user);

    List<BookingEntity> findByHotel(HotelEntity hotel);

    List<BookingEntity> findByHotelAndCreatedAtBetween(
            HotelEntity hotel,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime
    );
}
