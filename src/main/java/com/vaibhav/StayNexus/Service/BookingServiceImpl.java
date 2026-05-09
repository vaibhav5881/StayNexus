package com.vaibhav.StayNexus.Service;

import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.param.RefundCreateParams;
import com.vaibhav.StayNexus.Dto.BookingDTO;
import com.vaibhav.StayNexus.Dto.BookingRequest;
import com.vaibhav.StayNexus.Dto.GuestDTO;
import com.vaibhav.StayNexus.Dto.HotelReportDTO;
import com.vaibhav.StayNexus.Entities.*;
import com.vaibhav.StayNexus.Enums.BookingStatus;
import com.vaibhav.StayNexus.Exceptions.ResourceNotFoundException;
import com.vaibhav.StayNexus.Exceptions.UnAuthorisedException;
import com.vaibhav.StayNexus.Repositories.*;
import com.vaibhav.StayNexus.Service.Interfaces.BookingService;
import com.vaibhav.StayNexus.Service.Interfaces.CheckoutService;
import com.vaibhav.StayNexus.Strategy.PricingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import static com.vaibhav.StayNexus.Utils.AppUtils.getCurrentUser;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final InventoryRepository inventoryRepository;
    private final GuestRepository guestRepository;
    private final CheckoutService checkoutService;
    private final PricingService pricingService;
    private final ModelMapper modelMapper;

    @Value("$frontend.url")
    private String frontendUrl;

    @Override
    @Transactional
    public BookingDTO initialiseBooking(BookingRequest bookingRequest) {
        log.info("Initialising booking for hotel: {}, room: {}, dates: {}-{}",
                bookingRequest.getHotelId(), bookingRequest.getRoomId(),
                bookingRequest.getCheckInDate(), bookingRequest.getCheckOutDate());

        HotelEntity hotel = hotelRepository.findById(bookingRequest.getHotelId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Hotel not found with id: " + bookingRequest.getHotelId()
                ));

        RoomEntity room = roomRepository.findById(bookingRequest.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Room not found with id: " + bookingRequest.getRoomId()
                ));

        List<InventoryEntity> inventoryList = inventoryRepository.findAndLockAvailableInventory(
                room.getId(),
                bookingRequest.getCheckInDate(),
                bookingRequest.getCheckOutDate(),
                bookingRequest.getRoomsCount()
        );

        long daysCount = ChronoUnit.DAYS.between(
                bookingRequest.getCheckInDate(), bookingRequest.getCheckOutDate()) + 1;

        if(inventoryList.size() != daysCount) {
            throw new IllegalStateException(
                    "Room is not available for the selected dates. Please try different dates"
            );
        }

        inventoryRepository.initBooking(
                room.getId(),
                bookingRequest.getCheckInDate(),
                bookingRequest.getCheckOutDate(),
                bookingRequest.getRoomsCount()
        );

        BigDecimal priceForOneRoom = pricingService.calculateTotalPrice(inventoryList);
        BigDecimal totalPrice = priceForOneRoom.multiply(
                BigDecimal.valueOf(bookingRequest.getRoomsCount())
        );

        BookingEntity booking = BookingEntity.builder()
                .hotel(hotel)
                .room(room)
                .user(getCurrentUser())
                .checkInDate(bookingRequest.getCheckInDate())
                .checkOutDate(bookingRequest.getCheckOutDate())
                .roomsCount(bookingRequest.getRoomsCount())
                .bookingStatus(BookingStatus.RESERVED)
                .amount(totalPrice)
                .build();

        booking = bookingRepository.save(booking);
        log.info("Booking created with id: {}", booking.getId(), totalPrice);

        return modelMapper.map(booking , BookingDTO.class);
    }

    @Override
    @Transactional
    public BookingDTO addGuests(Long bookingId, List<GuestDTO> guestDtoList) {
        log.info("Adding {} guests to booking id: {}", guestDtoList.size(), bookingId);

        BookingEntity booking = findBookingById(bookingId);
        UserEntity currentUser = getCurrentUser();

        if(!currentUser.equals(booking.getUser())) {
            throw new UnAuthorisedException(
                    "Booking does not belong to this user with id: " + currentUser.getId()
            );
        }
        if(hasBookingExpired(booking)) {
            throw new IllegalStateException(
                    "Booking has expired. Please create a new booking."
            );
        }
        if(booking.getBookingStatus() != BookingStatus.RESERVED) {
            throw new IllegalStateException(
                    "Cannot add guests. Booking is in state: " +
                            booking.getBookingStatus()
            );
        }

        for(GuestDTO guestDTO : guestDtoList) {
            GuestEntity guest = modelMapper.map(guestDTO, GuestEntity.class);
            guest.setUser(currentUser);
            guest = guestRepository.save(guest);
            booking.getGuests().add(guest);
        }

        booking.setBookingStatus(BookingStatus.GUESTS_ADDED);
        booking = bookingRepository.save(booking);

        return modelMapper.map(booking , BookingDTO.class);
    }

    @Override
    @Transactional
    public String initiatePayments(Long bookingId) {
        log.info("Initiating payment for booking id: {}" , bookingId);

        BookingEntity booking = findBookingById(bookingId);
        UserEntity currentUser = getCurrentUser();

        if(!currentUser.equals(booking.getUser())) {
            throw new UnAuthorisedException(
                    "Booking does not belong to this user with id: " +
                            currentUser.getId());
        }
        if(hasBookingExpired(booking)) {
            throw new IllegalStateException(
                    "Booking has expired. Please create a new booking."
            );
        }

        String successUrl = frontendUrl + "/payments/" + bookingId + "/status";
        String failedUrl = frontendUrl + "/payments/" + bookingId + "/status";

        String sessionUrl = checkoutService.getCheckoutSession(booking , successUrl , failedUrl);

        booking.setBookingStatus(BookingStatus.PAYMENTS_PENDING);
        bookingRepository.save(booking);

        log.info("Payment initiated for booking id: {}", bookingId);
        return sessionUrl;
    }

    @Override
    @Transactional
    public void capturePayment(Event event) {
        log.info("Processing Stripe webhook event: {}", event.getType());

        if("checkout.session.completed".equals(event.getType())) {
            Session session = (Session) event.getDataObjectDeserializer()
                    .getObject().orElse(null);

            if(session == null) {
                log.warn("Could not deserialize session from event");
                return;
            }

            String sessionId = session.getId();
            BookingEntity booking = bookingRepository.findByPaymentSessionId(sessionId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Booking not found for session ID: " + sessionId
                    ));

            booking.setBookingStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(booking);

            inventoryRepository.findAndLockReservedInventory(
                    booking.getRoom().getId(),
                    booking.getCheckInDate(),
                    booking.getCheckOutDate(),
                    booking.getRoomsCount()
            );
            inventoryRepository.cancelBooking(
                    booking.getRoom().getId(),
                    booking.getCheckInDate(),
                    booking.getCheckOutDate(),
                    booking.getRoomsCount()
            );
            log.info("Booking {} confirmed successfully", booking.getId());
        }
        else {
            log.warn("Unhandled Stripe event type: {}", event.getType());
        }
    }

    @Override
    @Transactional
    public void cancelBooking(Long bookingId) {
        log.info("Cancelling booking id: {}", bookingId);

        BookingEntity booking = findBookingById(bookingId);
        UserEntity currentUser = getCurrentUser();

        if (!currentUser.equals(booking.getUser())) {
            throw new UnAuthorisedException(
                    "Booking does not belong to this user with id: " + currentUser.getId());
        }
        if (booking.getBookingStatus() != BookingStatus.CONFIRMED) {
            throw new IllegalStateException(
                    "Only CONFIRMED bookings can be cancelled. Current status: "
                            + booking.getBookingStatus());
        }

        booking.setBookingStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        inventoryRepository.findAndLockReservedInventory(
                booking.getRoom().getId(),
                booking.getCheckInDate(),
                booking.getCheckOutDate(),
                booking.getRoomsCount()
        );
        inventoryRepository.cancelBooking(
                booking.getRoom().getId(),
                booking.getCheckInDate(),
                booking.getCheckOutDate(),
                booking.getRoomsCount()
        );

        try {
            Session session = Session.retrieve(booking.getPaymentSessionId());
            RefundCreateParams refundParams = RefundCreateParams.builder()
                    .setPaymentIntent(session.getPaymentIntent())
                    .build();
            Refund.create(refundParams);
            log.info("Refund issued for booking id: {}", bookingId);
        } catch (StripeException e) {
            log.error("Stripe refund failed for booking {}: {}", bookingId, e.getMessage());
            throw new RuntimeException("Cancellation failed: " + e.getMessage(), e);
        }
    }

    @Override
    public BookingStatus getBookingStatus(Long bookingId) {
        BookingEntity booking = findBookingById(bookingId);
        UserEntity currentUser = getCurrentUser();

        if (!currentUser.equals(booking.getUser())) {
            throw new UnAuthorisedException(
                    "Booking does not belong to this user with id: " + currentUser.getId());
        }

        return booking.getBookingStatus();
    }

    @Override
    public List<BookingDTO> getMyBookings() {
        UserEntity currentUser = getCurrentUser();
        log.info("Getting all bookings for user id: {}", currentUser.getId());

        return bookingRepository.findByUser(currentUser)
                .stream()
                .map(booking -> modelMapper.map(booking, BookingDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDTO> getAllBookingsByHotelId(Long hotelId) {
        HotelEntity hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Hotel not found with id: " + hotelId));

        UserEntity currentUser = getCurrentUser();
        if (!currentUser.equals(hotel.getOwner())) {
            throw new AccessDeniedException(
                    "You are not the owner of hotel with id: " + hotelId);
        }

        log.info("Getting all bookings for hotel id: {}", hotelId);
        return bookingRepository.findByHotel(hotel)
                .stream()
                .map(booking -> modelMapper.map(booking, BookingDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public HotelReportDTO getHotelReport(Long hotelId,
                                         LocalDate startDate,
                                         LocalDate endDate) {
        HotelEntity hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Hotel not found with id: " + hotelId));

        UserEntity currentUser = getCurrentUser();
        if (!currentUser.equals(hotel.getOwner())) {
            throw new AccessDeniedException(
                    "You are not the owner of hotel with id: " + hotelId);
        }

        log.info("Generating report for hotel id: {} from {} to {}", hotelId, startDate, endDate);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<BookingEntity> bookings = bookingRepository
                .findByHotelAndCreatedAtBetween(hotel, startDateTime, endDateTime);

        long totalConfirmedBookings = bookings.stream()
                .filter(b -> b.getBookingStatus() == BookingStatus.CONFIRMED)
                .count();

        BigDecimal totalRevenue = bookings.stream()
                .filter(b -> b.getBookingStatus() == BookingStatus.CONFIRMED)
                .map(BookingEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal avgRevenue = totalConfirmedBookings == 0
                ? BigDecimal.ZERO
                : totalRevenue.divide(
                BigDecimal.valueOf(totalConfirmedBookings), RoundingMode.HALF_UP);

        return new HotelReportDTO(totalConfirmedBookings, totalRevenue, avgRevenue);
    }



    private BookingEntity findBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Booking not found with id: " + bookingId
                ));
    }

    public boolean hasBookingExpired(BookingEntity booking) {
        return booking.getCreatedAt().plusMinutes(10).isBefore(LocalDateTime.now());
    }
    
}




























