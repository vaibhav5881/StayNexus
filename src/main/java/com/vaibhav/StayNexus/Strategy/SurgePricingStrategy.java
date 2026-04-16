package com.vaibhav.StayNexus.Strategy;

import com.vaibhav.StayNexus.Entities.InventoryEntity;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;


@RequiredArgsConstructor
public class SurgePricingStrategy implements PricingStrategy {

    private final PricingStrategy wrapped;

    @Override
    public BigDecimal calculatePrice(InventoryEntity inventory) {

        BigDecimal price = wrapped.calculatePrice(inventory);

        return price.multiply(inventory.getSurgeFactor());
    }
}
