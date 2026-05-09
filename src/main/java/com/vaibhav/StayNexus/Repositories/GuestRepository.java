package com.vaibhav.StayNexus.Repositories;

import com.vaibhav.StayNexus.Entities.GuestEntity;
import com.vaibhav.StayNexus.Entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GuestRepository extends JpaRepository<GuestEntity, Long> {

    List<GuestEntity> findByUser(UserEntity user);
}
