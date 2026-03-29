package com.vaibhav.StayNexus.Service;

import com.vaibhav.StayNexus.Dto.ProfileUpdateRequestDTO;
import com.vaibhav.StayNexus.Dto.UserDTO;
import com.vaibhav.StayNexus.Entities.UserEntity;
import com.vaibhav.StayNexus.Exceptions.ResourceNotFoundException;
import com.vaibhav.StayNexus.Repositories.UserRepository;
import com.vaibhav.StayNexus.Service.Interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import static com.vaibhav.StayNexus.Utils.AppUtils.getCurrentUser;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    public UserEntity getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found with id: " + id));
    }

    @Override
    public UserDTO getMyProfile() {
        UserEntity currentUser = getCurrentUser();
        return modelMapper.map(currentUser, UserDTO.class);
    }

    @Override
    public void updateProfile(ProfileUpdateRequestDTO dto) {
        UserEntity currentUser = getCurrentUser();

        if(dto.getName() != null) currentUser.setName(dto.getName());
        if(dto.getGender() != null) currentUser.setGender(dto.getGender());
        if(dto.getDateOfBirth() != null) currentUser.setDateOfBirth(dto.getDateOfBirth());

        userRepository.save(currentUser);
    }

}







