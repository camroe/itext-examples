package com.cmr.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder

public class Item {
    private int quantity;
    private double price;
    private String unit;
    private String description;
}
