package com.vaibhav.StayNexus.Service;

import com.vaibhav.StayNexus.Dto.HotelDTO;
import com.vaibhav.StayNexus.Dto.HotelInfoDto;
import com.vaibhav.StayNexus.Dto.RoomDTO;
import com.vaibhav.StayNexus.Entities.HotelEntity;
import com.vaibhav.StayNexus.Entities.RoomEntity;
import com.vaibhav.StayNexus.Entities.UserEntity;
import com.vaibhav.StayNexus.Exceptions.ResourceNotFoundException;
import com.vaibhav.StayNexus.Exceptions.UnAuthorisedException;
import com.vaibhav.StayNexus.Repositories.HotelRepository;
import com.vaibhav.StayNexus.Repositories.RoomRepository;
import com.vaibhav.StayNexus.Service.Interfaces.HotelService;
import com.vaibhav.StayNexus.Service.Interfaces.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.vaibhav.StayNexus.Utils.AppUtils.getCurrentUser;

@Service
@RequiredArgsConstructor
@Slf4j
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final InventoryService inventoryService;
    private final ModelMapper modelMapper;

    @Override
    public HotelDTO createNewHotel(HotelDTO hotelDto) {
        log.info("Creating new hotel with name: {}", hotelDto.getName());

        UserEntity currentUser = getCurrentUser();
        HotelEntity hotel = modelMapper.map(hotelDto, HotelEntity.class);
        hotel.setOwner(currentUser);
        hotel.setActive(false);

        HotelEntity savedHotel = hotelRepository.save(hotel);
        log.info("Hotel created with id: {}", savedHotel.getId());

        return modelMapper.map(savedHotel, HotelDTO.class);

    }

    @Override
    public HotelDTO getHotelById(Long hotelId) {
        log.info("Getting hotel with id: {}", hotelId);

        HotelEntity hotel = findHotelById(hotelId);
        checkOwnership(hotel);

        return modelMapper.map(hotel , HotelDTO.class);
    }

    @Override
    public HotelDTO updateHotelById(Long hotelId, HotelDTO hotelDto) {
        log.info("Updating hotel with id: {}", hotelId);

        HotelEntity hotel = findHotelById(hotelId);
        checkOwnership(hotel);

        modelMapper.map(hotelDto, hotel);
        hotel.setId(hotelId);
        hotel.setOwner(getCurrentUser());

        HotelEntity updatedHotel = hotelRepository.save(hotel);
        return modelMapper.map(updatedHotel, HotelDTO.class);
    }

    @Override
    @Transactional
    public void deleteHotelById(Long hotelId) {
        log.info("Deleting hotel with id: {}", hotelId );

        HotelEntity hotel = findHotelById(hotelId);
        checkOwnership(hotel);

        List<RoomEntity> rooms = roomRepository.findByHotel(hotel);
        rooms.forEach(room -> inventoryService.deleteAllInventories(room));

        roomRepository.deleteAll(rooms);

        hotelRepository.deleteById(hotelId);
        log.info("Hotel with id: {} deleted successfully", hotelId);
    }

    @Override
    @Transactional
    public void activateHotel(Long hotelId) {
        log.info("Activating hotel with id: {}", hotelId);

        HotelEntity hotel = findHotelById(hotelId);
        checkOwnership(hotel);

        hotel.setActive(true);
        hotelRepository.save(hotel);

        List<RoomEntity> rooms = roomRepository.findByHotel(hotel);
        rooms.forEach(room -> inventoryService.initializeRoomForAYear(room));

        log.info("Hotel with id: {} activated. Initialized inventory for {} rooms", hotelId, rooms.size());
    }

    @Override
    public List<HotelDTO> getAllHotels() {
        UserEntity currentUser = getCurrentUser();
        log.info("Getting all hotels for owner id: {}", currentUser.getId());

        List<HotelEntity> hotels = hotelRepository.findByOwner(currentUser);
        return hotels.stream()
                .map(hotel -> modelMapper.map(hotel , HotelDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public HotelInfoDto getHotelInfoById(Long hotelId) {
        log.info("Getting hotel info for hotel id: {}", hotelId);

        HotelEntity hotel = findHotelById(hotelId);

        HotelInfoDto hotelInfoDto = modelMapper.map(hotel , HotelInfoDto.class);

        List<RoomEntity> rooms = roomRepository.findByHotel(hotel);
        List<RoomDTO> roomDTOs = rooms.stream()
                .map(room -> modelMapper.map(room , RoomDTO.class))
                .collect(Collectors.toList());

        hotelInfoDto.setRooms(roomDTOs);
        return hotelInfoDto;
    }


    private HotelEntity findHotelById(Long hotelId) {
        return hotelRepository.findById(hotelId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Hotel not found with id: " + hotelId));
    }

    private void checkOwnership(HotelEntity hotel) {
        UserEntity currentUser = getCurrentUser();
        if(!currentUser.equals(hotel.getOwner())) {
            throw new UnAuthorisedException(
                    "You are not the owner of hotel with id: " + hotel.getId()
            );
        }
    }
}

















































































