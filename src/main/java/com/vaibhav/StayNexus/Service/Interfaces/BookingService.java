package com.vaibhav.StayNexus.Service.Interfaces;

import com.stripe.model.Event;
import com.vaibhav.StayNexus.Dto.BookingDTO;
import com.vaibhav.StayNexus.Dto.BookingRequest;
import com.vaibhav.StayNexus.Dto.GuestDTO;
import com.vaibhav.StayNexus.Dto.HotelReportDTO;
import com.vaibhav.StayNexus.Enums.BookingStatus;
import java.time.LocalDate;
import java.util.List;

public interface BookingService {

    BookingDTO initialiseBooking(BookingRequest bookingRequest);

    BookingDTO addGuests(Long bookingId, List<GuestDTO> guestDtoList);

    String initiatePayments(Long bookingId);

    void capturePayment(Event event);

    void cancelBooking(Long bookingId);

    BookingStatus getBookingStatus(Long bookingId);

    List<BookingDTO> getMyBookings();

    List<BookingDTO> getAllBookingsByHotelId(Long hotelId);

    HotelReportDTO getHotelReport(Long hotelId, LocalDate startDate, LocalDate endDate);
}


















