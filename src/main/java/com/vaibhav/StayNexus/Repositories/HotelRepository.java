package com.vaibhav.StayNexus.Repositories;

import com.vaibhav.StayNexus.Entities.HotelEntity;
import com.vaibhav.StayNexus.Entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HotelRepository extends JpaRepository<HotelEntity, Long>{

    List<HotelEntity> findByOwner(UserEntity owner);
}

