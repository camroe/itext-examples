package com.cmr.factory;

import com.cmr.domain.PurchaseOrder;
import lombok.Builder;

import java.util.Date;

public class PurchaseOrderFactory {

    /**
     * This method create a standard, error free, normal, nominal purchase order. This is meant to be used
     * as a method to create test purchase orders that can be used for success testing or mutated into
     * abhorrent purchase orders for failure testing.
     * @param poNumber the purchase order number as a String
     * @return a complete valid purcharse order entity
     */
    public static PurchaseOrder createTestPO(String poNumber) {
    return (PurchaseOrder.builder()
            .poNumber(poNumber)
            .poDate(new Date(System.currentTimeMillis()))
            .build());

    }
}
