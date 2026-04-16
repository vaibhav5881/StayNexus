package com.vaibhav.StayNexus.Service;

import com.vaibhav.StayNexus.Entities.HotelEntity;
import com.vaibhav.StayNexus.Entities.HotelMinPriceEntity;
import com.vaibhav.StayNexus.Entities.InventoryEntity;
import com.vaibhav.StayNexus.Repositories.HotelMinPriceRepository;
import com.vaibhav.StayNexus.Repositories.HotelRepository;
import com.vaibhav.StayNexus.Repositories.InventoryRepository;
import com.vaibhav.StayNexus.Strategy.PricingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PricingUpdateService {

    private final HotelRepository hotelRepository;
    private final InventoryRepository inventoryRepository;
    private final HotelMinPriceRepository hotelMinPriceRepository;
    private final PricingService pricingService;

    @Scheduled(cron = "0 0 * * * *")
    public void updatePrices() {
        log.info("Starting hourly price update...");

        int page = 0;
        int batchSize = 100;

        while (true) {
            Page<HotelEntity> hotelPage =
                    hotelRepository.findAll(PageRequest.of(page, batchSize));

            if(hotelPage.isEmpty()) break;

            hotelPage.getContent().forEach(this::updateHotelPrices);
            page++;
        }

        log.info("Hourly price update completed.");
    }

    private void updateHotelPrices(HotelEntity hotel) {
        log.info("Updating prices for hotel id: {}", hotel.getId());

        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusYears(1);

        List<InventoryEntity> inventoryList =
                inventoryRepository.findByHotelAndDateBetween(hotel , today , endDate);

        updateInventoryPrices(inventoryList);

        updateHotelMinPrice(hotel , inventoryList);
    }

    private void updateInventoryPrices(List<InventoryEntity> inventoryList) {
        inventoryList.forEach(inventory -> {
            BigDecimal dynamicPrice = pricingService.calculateDynamicPricing(inventory);
            inventory.setPrice(dynamicPrice);
        });
        inventoryRepository.saveAll(inventoryList);
    }

    private void updateHotelMinPrice(HotelEntity hotel ,
                                     List<InventoryEntity> inventoryList) {

        Map<LocalDate , Optional<BigDecimal>> dailyMinPrices = inventoryList.stream()
                .collect(Collectors.groupingBy(
                        InventoryEntity::getDate,
                        Collectors.mapping(
                                InventoryEntity::getPrice,
                                Collectors.minBy(Comparator.naturalOrder())
                        )
                ));

        List<HotelMinPriceEntity> hotelMinPrices = new ArrayList<>();
        dailyMinPrices.forEach((date, optionalMinPrice) -> {
            BigDecimal minPrice = optionalMinPrice.orElse(BigDecimal.ZERO);

            HotelMinPriceEntity hotelMinPrice =
                    hotelMinPriceRepository.findByHotelAndDate(hotel, date)
                            .orElse(new HotelMinPriceEntity(hotel , date));

            hotelMinPrice.setPrice(minPrice);
            hotelMinPrices.add(hotelMinPrice);
        });

        hotelMinPriceRepository.saveAll(hotelMinPrices);
    }

}


































