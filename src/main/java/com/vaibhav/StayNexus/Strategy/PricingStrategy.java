package com.vaibhav.StayNexus.Strategy;

import com.vaibhav.StayNexus.Entities.InventoryEntity;

import java.math.BigDecimal;

public interface PricingStrategy {

    BigDecimal calculatePrice(InventoryEntity inventory);
}
