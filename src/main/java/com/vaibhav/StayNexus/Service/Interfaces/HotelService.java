package com.vaibhav.StayNexus.Service.Interfaces;

import com.vaibhav.StayNexus.Dto.HotelDTO;
import com.vaibhav.StayNexus.Dto.HotelInfoDto;

import java.util.List;

public interface HotelService {

    HotelDTO createNewHotel(HotelDTO hotelDTO);

    HotelDTO getHotelById(Long hotelId);

    HotelDTO updateHotelById(Long hotelId, HotelDTO hotelDTO);

    void deleteHotelById(Long hotelId);

    void activateHotel(Long hotelId);

    List<HotelDTO> getAllHotels();

    HotelInfoDto getHotelInfoById(Long hotelId);

}
