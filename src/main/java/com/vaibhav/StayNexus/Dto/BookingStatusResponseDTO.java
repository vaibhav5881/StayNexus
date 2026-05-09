package com.vaibhav.StayNexus.Dto;

import com.vaibhav.StayNexus.Enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BookingStatusResponseDTO {
    private BookingStatus bookingStatus;
}
