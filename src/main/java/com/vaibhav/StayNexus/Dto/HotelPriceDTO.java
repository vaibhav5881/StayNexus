package com.vaibhav.StayNexus.Dto;

import com.vaibhav.StayNexus.Entities.HotelContactInfo;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class HotelPriceDTO {
    private Long id;
    private String name;
    private String city;
    private String[] photos;
    private String[] amenities;
    private HotelContactInfo contactInfo;
    private BigDecimal price;
}
