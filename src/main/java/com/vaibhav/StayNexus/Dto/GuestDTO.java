package com.vaibhav.StayNexus.Dto;

import com.vaibhav.StayNexus.Enums.Gender;
import lombok.Data;

@Data
public class GuestDTO {
    private Long id ;
    private String name;
    private Gender gender;
    private Integer age;
}
