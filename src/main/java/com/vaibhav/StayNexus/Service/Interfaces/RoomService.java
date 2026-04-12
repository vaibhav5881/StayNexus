package com.vaibhav.StayNexus.Service.Interfaces;

import com.vaibhav.StayNexus.Dto.RoomDTO;

import java.util.List;

public interface RoomService {

    RoomDTO createNewRoom(Long hotelId, RoomDTO roomDto);

    RoomDTO getRoomById(Long roomId);

    List<RoomDTO> getAllRoomsInHotel(Long hotelId);

    RoomDTO updateRoomById(Long hotelId, Long roomId, RoomDTO roomDto);

    void deletedRoomById(Long roomId);
}
