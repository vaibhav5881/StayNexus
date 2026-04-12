package com.vaibhav.StayNexus.Repositories;

import com.vaibhav.StayNexus.Entities.HotelEntity;
import com.vaibhav.StayNexus.Entities.RoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<RoomEntity , Long> {

    List<RoomEntity> findByHotel(HotelEntity hotel);
}
