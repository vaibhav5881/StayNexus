package com.vaibhav.StayNexus.Controller;

import com.vaibhav.StayNexus.Dto.HotelInfoDto;
import com.vaibhav.StayNexus.Dto.HotelPriceDTO;
import com.vaibhav.StayNexus.Dto.HotelSearchRequest;
import com.vaibhav.StayNexus.Service.Interfaces.HotelService;
import com.vaibhav.StayNexus.Service.Interfaces.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hotels")
@RequiredArgsConstructor
@Tag(name = "Hotel Browse", description = "Public: search and browse hotels")
public class HotelBrowseController {

    private final HotelService hotelService;
    private final InventoryService inventoryService;

    @GetMapping("/search")
    @Operation(summary = "Search for available hotels by city and dates")
    public ResponseEntity<Page<HotelPriceDTO>> searchHotels(
            @RequestBody HotelSearchRequest hotelSearchRequest) {
        return ResponseEntity.ok(inventoryService.searchHotels(hotelSearchRequest));
    }

    @GetMapping("/{hotelId}/info")
    @Operation(summary = "Get full hotel details including room types")
    public ResponseEntity<HotelInfoDto> getHotelInfo(@PathVariable Long hotelId) {
        return ResponseEntity.ok(hotelService.getHotelInfoById(hotelId));
    }
}






























