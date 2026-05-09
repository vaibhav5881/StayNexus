package com.vaibhav.StayNexus.Service.Interfaces;

import com.vaibhav.StayNexus.Dto.GuestDTO;

import java.util.List;

public interface GuestService {

    List<GuestDTO> getAllGuests();

    GuestDTO addNewGuest(GuestDTO guestDTO);

    void updateGuest(Long guestId, GuestDTO guestDTO);

    void deleteGuest(Long guestId);
}
