package com.vaibhav.StayNexus.Dto;

import com.vaibhav.StayNexus.Enums.Gender;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ProfileUpdateRequestDTO {
    private String name;
    private Gender gender;
    private LocalDate dateOfBirth;
}
