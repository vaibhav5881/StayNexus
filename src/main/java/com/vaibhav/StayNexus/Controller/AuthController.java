package com.vaibhav.StayNexus.Controller;

import com.vaibhav.StayNexus.Dto.LoginDTO;
import com.vaibhav.StayNexus.Dto.LoginResponseDTO;
import com.vaibhav.StayNexus.Dto.SignUpRequestDTO;
import com.vaibhav.StayNexus.Dto.UserDTO;
import com.vaibhav.StayNexus.Security.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "User Authentication", description = "Signup, login and token refresh")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    @Operation(summary = "Register a new user account")
    public ResponseEntity<UserDTO> signup(@RequestBody SignUpRequestDTO signUpRequestDTO) {
        return new ResponseEntity<>(authService.signUp(signUpRequestDTO), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @Operation(summary = "Login and receive access + refresh tokens")
    public ResponseEntity<LoginResponseDTO> login(
            @RequestBody LoginDTO loginDTO,
            HttpServletResponse response
            ) {
        String[] tokens = authService.login(loginDTO);

        Cookie refreshTokenCookie = new Cookie("refreshToken", tokens[1]);
        refreshTokenCookie.setHttpOnly(true);
        response.addCookie(refreshTokenCookie);
        return ResponseEntity.ok(new LoginResponseDTO(tokens[0]));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Get a new access token using the refresh cookie")
    public ResponseEntity<LoginResponseDTO> refresh(HttpServletRequest request) {
        if(request.getCookies() == null) {
            throw new AuthenticationServiceException("Refresh token cookie not found");
        }

        String refreshToken = Arrays.stream(request.getCookies())
                .filter(cookie -> "refreshToken".equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElseThrow(() ->
                        new AuthenticationServiceException("Refresh token cookie not found"));

        String newAccessToken = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(new LoginResponseDTO(newAccessToken));
    }
}












































