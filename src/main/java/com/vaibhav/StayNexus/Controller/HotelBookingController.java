package com.vaibhav.StayNexus.Controller;

import com.vaibhav.StayNexus.Dto.*;
import com.vaibhav.StayNexus.Service.Interfaces.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Booking Flow", description = "Book hotels: reserve, pay, cancel")
public class HotelBookingController {

    private final BookingService bookingService;

    @PostMapping("/init")
    @Operation(summary = "Step 1: Initialize a new booking (reserves the room)")
    public ResponseEntity<BookingDTO> initialiseBooking(
            @RequestBody BookingRequest bookingRequest) {
        return ResponseEntity.ok(bookingService.initialiseBooking(bookingRequest));
    }

    @PostMapping("/{bookingId}/addGuests")
    @Operation(summary = "Step 2: Add guests to a reserved booking")
    public ResponseEntity<BookingDTO> addGuests(
            @PathVariable Long bookingId,
            @RequestBody List<GuestDTO> guestDtoList) {
        return ResponseEntity.ok(bookingService.addGuests(bookingId, guestDtoList));
    }

    @PostMapping("/{bookingId}/payments")
    @Operation(summary = "Step 3: Initiate payment — returns Stripe checkout URL")
    public ResponseEntity<BookingPaymentInitResponseDTO> initiatePayments(
            @PathVariable Long bookingId) {
        String sessionUrl = bookingService.initiatePayments(bookingId);
        return ResponseEntity.ok(new BookingPaymentInitResponseDTO(sessionUrl));
    }

    @GetMapping("/{bookingId}/status")
    @Operation(summary = "Check booking status — poll after returning from Stripe")
    public ResponseEntity<BookingStatusResponseDTO> getBookingStatus(
            @PathVariable Long bookingId) {
        return ResponseEntity.ok(
                new BookingStatusResponseDTO(bookingService.getBookingStatus(bookingId)));
    }

    @PostMapping("/{bookingId}/cancel")
    @Operation(summary = "Cancel a confirmed booking (Stripe refund issued)")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long bookingId) {
        bookingService.cancelBooking(bookingId);
        return ResponseEntity.noContent().build();
    }
}