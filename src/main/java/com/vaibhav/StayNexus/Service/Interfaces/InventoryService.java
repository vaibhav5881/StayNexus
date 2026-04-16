package com.vaibhav.StayNexus.Service.Interfaces;

import com.vaibhav.StayNexus.Dto.HotelPriceDTO;
import com.vaibhav.StayNexus.Dto.HotelSearchRequest;
import com.vaibhav.StayNexus.Dto.InventoryDTO;
import com.vaibhav.StayNexus.Dto.UpdateInventoryRequestDTO;
import com.vaibhav.StayNexus.Entities.RoomEntity;
import org.springframework.data.domain.Page;

import java.util.List;

public interface InventoryService {

    void initializeRoomForAYear(RoomEntity room);

    void deleteAllInventories(RoomEntity room);

    Page<HotelPriceDTO> searchHotels(HotelSearchRequest hotelSearchRequest);

    List<InventoryDTO> getAllInventoryByRoom(Long roomId);

    void updateInventory(Long roomId, UpdateInventoryRequestDTO updateInventoryRequestDTO);
}
