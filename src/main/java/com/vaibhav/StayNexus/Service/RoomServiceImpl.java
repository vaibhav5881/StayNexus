package com.vaibhav.StayNexus.Service;

import com.vaibhav.StayNexus.Dto.RoomDTO;
import com.vaibhav.StayNexus.Entities.HotelEntity;
import com.vaibhav.StayNexus.Entities.RoomEntity;
import com.vaibhav.StayNexus.Exceptions.ResourceNotFoundException;
import com.vaibhav.StayNexus.Repositories.HotelRepository;
import com.vaibhav.StayNexus.Repositories.RoomRepository;
import com.vaibhav.StayNexus.Service.Interfaces.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final InventoryService inventoryService;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public RoomDTO createNewRoom(Long hotelId, RoomDTO roomDto) {
        log.info("Creating new room in hotel id: {}", hotelId);

        HotelEntity hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Hotel not found with id: " + hotelId));

        RoomEntity room = modelMapper.map(roomDto, RoomEntity.class);
        room.setHotel(hotel);

        RoomEntity savedRoom = roomRepository.save(room);

        if(hotel.getActive()) {
            log.info("Hotel is active, initializing inventory for new room id: {}", savedRoom.getId());
            inventoryService.initializeRoomForAYear(savedRoom);
        }

        return modelMapper.map(savedRoom, RoomDTO.class);
    }

    @Override
    public RoomDTO getRoomById(Long roomId) {
        log.info("Getting room with id: {}", roomId);

        RoomEntity room = roomRepository.findById(roomId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Room not found with id: " + roomId));

        return modelMapper.map(room , RoomDTO.class);
    }

    @Override
    public List<RoomDTO> getAllRoomsInHotel(Long hotelId) {
        log.info("Getting all rooms for hotel id: {}", hotelId);

        HotelEntity hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Hotel not found with id: " + hotelId));

        return roomRepository.findByHotel(hotel)
                .stream()
                .map(room -> modelMapper.map(room , RoomDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public RoomDTO updateRoomById(Long hotelId , Long roomId , RoomDTO roomDto) {
        log.info("Updating room id: {} in hotel id: {}", roomId , hotelId);

        HotelEntity hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Hotel not found with id: " + hotelId));

        RoomEntity room = roomRepository.findById(roomId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Room not found with id: " + roomId ));

        modelMapper.map(roomDto, room);
        room.setId(roomId);
        room.setHotel(hotel);

        RoomEntity updatedRoom = roomRepository.save(room);
        return modelMapper.map(updatedRoom, RoomDTO.class);
    }

    @Override
    @Transactional
    public void deletedRoomById(Long roomId) {
        log.info("Deleting room with id: {}", roomId);

        RoomEntity room = roomRepository.findById(roomId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Room not found with id: " + roomId));

        inventoryService.deleteAllInventories(room);

        roomRepository.deleteById(roomId);
        log.info("Room with id: {} deleted successfully", roomId);
    }


}






























