package com.vaibhav.StayNexus.Controller;

import com.vaibhav.StayNexus.Dto.InventoryDTO;
import com.vaibhav.StayNexus.Dto.UpdateInventoryRequestDTO;
import com.vaibhav.StayNexus.Service.Interfaces.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/inventory")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Admin Inventory", description = "Admin: manage room inventory and pricing")
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/rooms/{roomId}")
    @Operation(summary = "View all inventory for a room (day by day)")
    public ResponseEntity<List<InventoryDTO>> getAllInventoryByRoom(@PathVariable Long roomId) {
        return ResponseEntity.ok(inventoryService.getAllInventoryByRoom(roomId));
    }

    @PatchMapping("/rooms/{roomId}")
    @Operation(summary = "Update surge pricing or close/ropen rooms for a date range")
    public ResponseEntity<Void> updateInventory(
            @PathVariable Long roomId,
            @RequestBody UpdateInventoryRequestDTO updateInventoryRequestDto
            ) {
        inventoryService.updateInventory(roomId, updateInventoryRequestDto);
        return ResponseEntity.noContent().build();
    }
}

























