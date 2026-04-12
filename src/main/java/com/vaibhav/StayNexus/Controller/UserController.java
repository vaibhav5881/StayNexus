package com.vaibhav.StayNexus.Controller;

import com.vaibhav.StayNexus.Dto.ProfileUpdateRequestDTO;
import com.vaibhav.StayNexus.Dto.UserDTO;
import com.vaibhav.StayNexus.Service.Interfaces.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "User Profile", description = "Manage user profile")
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    @Operation(summary = "Get my profile")
    public ResponseEntity<UserDTO> getMyProfile() {
        return ResponseEntity.ok(userService.getMyProfile());
    }

    @PatchMapping("/profile")
    @Operation(summary = "Update my profile")
    public ResponseEntity<Void> updateProfile(@RequestBody ProfileUpdateRequestDTO dto) {
        userService.updateProfile(dto);
        return ResponseEntity.noContent().build();
    }
}




























