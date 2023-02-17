package com.cmr.domain;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;

@Data
@Builder
public class PurchaseOrder {
    private String poNumber;
    private Date poDate;
    private ArrayList<Item> items;

}
