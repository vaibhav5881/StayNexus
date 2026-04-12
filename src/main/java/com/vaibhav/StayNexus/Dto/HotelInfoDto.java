package com.vaibhav.StayNexus.Dto;

import com.vaibhav.StayNexus.Entities.HotelContactInfo;
import lombok.Data;

import java.util.List;

@Data
public class HotelInfoDto {
    private Long id;
    private String name;
    private String city;
    private String[] photos;
    private String[] amenities;
    private HotelContactInfo contactInfo;
    private List<RoomDTO> rooms;
}
