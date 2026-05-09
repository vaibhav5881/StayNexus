package com.vaibhav.StayNexus.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class HotelReportDTO {
    private Long totalConfirmedBookings;
    private BigDecimal totalRevenue;
    private BigDecimal avgRevenue;
}
