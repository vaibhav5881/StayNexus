package com.vaibhav.StayNexus.Security;

import com.vaibhav.StayNexus.Dto.LoginDTO;
import com.vaibhav.StayNexus.Dto.SignUpRequestDTO;
import com.vaibhav.StayNexus.Dto.UserDTO;
import com.vaibhav.StayNexus.Entities.UserEntity;
import com.vaibhav.StayNexus.Enums.Role;
import com.vaibhav.StayNexus.Exceptions.ResourceNotFoundException;
import com.vaibhav.StayNexus.Repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;

    public UserDTO signUp(SignUpRequestDTO signUpRequestDTO) {
        boolean emailExists = userRepository.findByEmail(signUpRequestDTO.getEmail()).isPresent();

        if(emailExists) {
            throw new RuntimeException("User already exists with email: "
                    + signUpRequestDTO.getEmail());
        }

        UserEntity newUser = modelMapper.map(signUpRequestDTO , UserEntity.class);

        newUser.setPassword(passwordEncoder.encode(signUpRequestDTO.getPassword()));

        newUser.setRoles(Set.of(Role.GUEST));

        newUser = userRepository.save(newUser);

        return modelMapper.map(newUser , UserDTO.class);
    }

    public String[] login(LoginDTO loginDTO) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDTO.getEmail(),
                        loginDTO.getPassword()
                )
        );

        UserEntity user = (UserEntity) authentication.getPrincipal();

        String[] tokens = new String[2];
        tokens[0] = jwtService.generateAccessToken(user);
        tokens[1] = jwtService.generateRefreshToken(user);

        return tokens;
    }

    public String refreshToken(String refreshToken) {
        Long userId = jwtService.getUserIdFromToken(refreshToken);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found with id: " + userId));

        return jwtService.generateAccessToken(user);
    }
}





























