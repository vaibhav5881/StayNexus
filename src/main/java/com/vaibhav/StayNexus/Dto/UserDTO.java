package com.vaibhav.StayNexus.Dto;

import com.vaibhav.StayNexus.Enums.Gender;
import com.vaibhav.StayNexus.Enums.Role;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
public class UserDTO {
    private Long id;
    private String email;
    private String name;
    private LocalDate dateOfBirth;
    private Gender gender;
    private Set<Role> roles;
}
