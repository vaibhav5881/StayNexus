package com.vaibhav.StayNexus.Dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class InventoryDTO {
    private Long id;
    private LocalDate date;
    private Boolean closed;
    private Integer totalCount;
    private Integer bookCount;
    private BigDecimal surgeFactor;
    private BigDecimal price;
}
