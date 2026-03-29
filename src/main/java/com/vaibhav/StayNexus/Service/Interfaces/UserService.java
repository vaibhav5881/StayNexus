package com.vaibhav.StayNexus.Service.Interfaces;

import com.vaibhav.StayNexus.Dto.ProfileUpdateRequestDTO;
import com.vaibhav.StayNexus.Dto.UserDTO;
import com.vaibhav.StayNexus.Entities.UserEntity;

public interface UserService {
    UserEntity getUserById(Long id);
    UserDTO getMyProfile();
    void updateProfile(ProfileUpdateRequestDTO dto);
}
