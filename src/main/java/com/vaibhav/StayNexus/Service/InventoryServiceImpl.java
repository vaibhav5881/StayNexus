package com.vaibhav.StayNexus.Service;

import com.vaibhav.StayNexus.Dto.HotelPriceDTO;
import com.vaibhav.StayNexus.Dto.HotelSearchRequest;
import com.vaibhav.StayNexus.Dto.InventoryDTO;
import com.vaibhav.StayNexus.Dto.UpdateInventoryRequestDTO;
import com.vaibhav.StayNexus.Entities.HotelMinPriceEntity;
import com.vaibhav.StayNexus.Entities.InventoryEntity;
import com.vaibhav.StayNexus.Entities.RoomEntity;
import com.vaibhav.StayNexus.Entities.UserEntity;
import com.vaibhav.StayNexus.Exceptions.ResourceNotFoundException;
import com.vaibhav.StayNexus.Repositories.HotelMinPriceRepository;
import com.vaibhav.StayNexus.Repositories.InventoryRepository;
import com.vaibhav.StayNexus.Repositories.RoomRepository;
import com.vaibhav.StayNexus.Service.Interfaces.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import static com.vaibhav.StayNexus.Utils.AppUtils.getCurrentUser;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final HotelMinPriceRepository hotelMinPriceRepository;
    private final RoomRepository roomRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public void initializeRoomForAYear(RoomEntity room) {
        log.info("Initializing inventory for room id: {}", room.getId());

        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusYears(1);

        for (LocalDate date = today; !date.isAfter(endDate); date = date.plusDays(1)) {
            InventoryEntity inventory = InventoryEntity.builder()
                    .hotel(room.getHotel())
                    .room(room)
                    .date(date)
                    .bookCount(0)
                    .reservedCount(0)
                    .totalCount(room.getTotalCount())
                    .surgeFactor(BigDecimal.ONE)
                    .price(room.getBasePrice())
                    .city(room.getHotel().getCity())
                    .closed(false)
                    .build();

            inventoryRepository.save(inventory);
        }

        log.info("Initialized {} inventory rows for room id: {}",
                ChronoUnit.DAYS.between(LocalDate.now(), endDate) + 1 , room.getId());
    }

    @Override
    public void deleteAllInventories(RoomEntity room) {
        log.info("Deleting all inventory for room id: {}", room.getId());
        inventoryRepository.deleteByRoom(room);
    }

    @Override
    public Page<HotelPriceDTO> searchHotels(HotelSearchRequest request) {
        log.info("Searching hotels in {} from {} to {} for {} rooms",
                request.getCity(), request.getStartDate(),
                request.getEndDate(), request.getRoomsCount());

        Pageable pageable = PageRequest.of(
                request.getPage() != null ? request.getPage() : 0,
                request.getSize() != null ? request.getSize() : 10
        );

        long dateCount = ChronoUnit.DAYS.between(
                request.getStartDate(), request.getEndDate()) + 1;

        Page<HotelMinPriceEntity> hotelPage =
                hotelMinPriceRepository.findHotelsWithAvailableInventory(
                        request.getCity(),
                        request.getStartDate(),
                        request.getEndDate(),
                        request.getRoomsCount(),
                        dateCount,
                        pageable
                );

        return hotelPage.map(hotelMinPrice ->
                modelMapper.map(hotelMinPrice, HotelPriceDTO.class));
    }

    @Override
    public List<InventoryDTO> getAllInventoryByRoom(Long roomId) {
        log.info("Getting inventory for room id: {}", roomId);

        RoomEntity room = roomRepository.findById(roomId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Room not found with id: " + roomId));

        UserEntity currentUser = getCurrentUser();
        if (!currentUser.equals(room.getHotel().getOwner())) {
            throw new AccessDeniedException(
                    "You are not the owner of room with id: " + roomId);
        }

        return inventoryRepository.findByRoomOrderByDate(room)
                .stream()
                .map(inventory -> modelMapper.map(inventory, InventoryDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateInventory(Long roomId, UpdateInventoryRequestDTO dto) {
        log.info("Updating inventory for room id: {} from {} to {}",
                roomId, dto.getStartDate(), dto.getEndDate());

        RoomEntity room = roomRepository.findById(roomId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Room not found with id: " + roomId));

        UserEntity currentUser = getCurrentUser();
        if (!currentUser.equals(room.getHotel().getOwner())) {
            throw new AccessDeniedException(
                    "You are not the owner of room with id: " + roomId);
        }

        inventoryRepository.getInventoryAndLockBeforeUpdate(
                roomId, dto.getStartDate(), dto.getEndDate());

        inventoryRepository.updateInventory(
                roomId,
                dto.getStartDate(),
                dto.getEndDate(),
                dto.getClosed(),
                dto.getSurgeFactor()
        );
    }
}

























