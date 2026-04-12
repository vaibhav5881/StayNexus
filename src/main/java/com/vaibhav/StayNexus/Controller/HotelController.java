package com.vaibhav.StayNexus.Controller;

import com.vaibhav.StayNexus.Dto.HotelDTO;
import com.vaibhav.StayNexus.Dto.RoomDTO;
import com.vaibhav.StayNexus.Service.Interfaces.HotelService;
import com.vaibhav.StayNexus.Service.Interfaces.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/hotels")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Hotel Management", description = "Admin: create and manage hotels and rooms")
public class HotelController {

    private final HotelService hotelService;
    private final RoomService roomService;


    @PostMapping
    @Operation(summary = "Create a new hotel")
    public ResponseEntity<HotelDTO> createNewHotel(@RequestBody HotelDTO hotelDto) {
        log.info("Request to create hotel: {}", hotelDto.getName());
        return new ResponseEntity<>(hotelService.createNewHotel(hotelDto), HttpStatus.CREATED);
    }

    @GetMapping("/{hotelId}")
    @Operation(summary = "Get hotel details by ID")
    public ResponseEntity<HotelDTO> getHotelById(@PathVariable Long hotelId) {
        return ResponseEntity.ok(hotelService.getHotelById(hotelId));
    }

    @PutMapping("/{hotelId}")
    @Operation(summary = "Update hotel details")
    public ResponseEntity<HotelDTO> updateHotelById(@PathVariable Long hotelId, @RequestBody HotelDTO hotelDto) {
        return ResponseEntity.ok(hotelService.updateHotelById(hotelId, hotelDto));
    }

    @DeleteMapping("/{hotelId}")
    @Operation(summary = "Delete a hotel and all its rooms and inventory")
    public ResponseEntity<Void> deleteHotelById(@PathVariable Long hotelId) {
        hotelService.deleteHotelById(hotelId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{hotelId}/activate")
    @Operation(summary = "Activate a hotel - makes it searchable and initializes inventory")
    public ResponseEntity<Void> activateHotel(@PathVariable Long hotelId) {
        hotelService.activateHotel(hotelId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Get all hotels owned by the logged-in manager")
    public ResponseEntity<List<HotelDTO>> getAllHotels() {
        return ResponseEntity.ok(hotelService.getAllHotels());
    }

    @PostMapping("/{hotelId}/rooms")
    @Operation(summary = "Add a new room type to a hotel")
    public ResponseEntity<RoomDTO> createNewRoom(@PathVariable Long hotelId,
                                                 @RequestBody RoomDTO roomDto) {
        return new ResponseEntity<>(roomService.createNewRoom(hotelId, roomDto), HttpStatus.CREATED);
    }

    @GetMapping("/{hotelId}/rooms")
    @Operation(summary = "Get all room types in a hotel")
    public ResponseEntity<List<RoomDTO>> getAllRooms(@PathVariable Long hotelId) {
        return ResponseEntity.ok(roomService.getAllRoomsInHotel(hotelId));
    }

    @GetMapping("/{hotelId}/rooms/{roomId}")
    @Operation(summary = "Get a specific room by ID")
    public ResponseEntity<RoomDTO> getRoomById(@PathVariable Long hotelId,
                                               @PathVariable Long roomId) {
        return ResponseEntity.ok(roomService.getRoomById(roomId));
    }

    @PutMapping("/{hotelId}/rooms/{roomId}")
    @Operation(summary = "Update a room's details")
    public ResponseEntity<RoomDTO> updateRoom(@PathVariable Long hotelId,
                                              @PathVariable Long roomId,
                                              @RequestBody RoomDTO roomDto) {
        return ResponseEntity.ok(roomService.updateRoomById(hotelId, roomId, roomDto));
    }

    @DeleteMapping("/{hotelId}/rooms/{roomId}")
    @Operation(summary = "Delete a room and its inventory")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long hotelId,
                                           @PathVariable Long roomId) {
        roomService.deletedRoomById(roomId);
        return ResponseEntity.noContent().build();
    }

}
































