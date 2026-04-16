package com.vaibhav.StayNexus.Strategy;

import com.vaibhav.StayNexus.Entities.InventoryEntity;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@RequiredArgsConstructor
public class UrgencyPricingStrategy implements PricingStrategy {

    private final PricingStrategy wrapped;

    @Override
    public BigDecimal calculatePrice(InventoryEntity inventory) {

        BigDecimal price = wrapped.calculatePrice(inventory);

        LocalDate today = LocalDate.now();
        LocalDate inventoryDate = inventory.getDate();

        boolean isWithinSevenDays =
                !inventoryDate.isBefore(today) &&
                        inventoryDate.isBefore(today.plusDays(7));

        if(isWithinSevenDays) {
            price = price.multiply(BigDecimal.valueOf(1.15));
        }

        return price;

    }
}





















