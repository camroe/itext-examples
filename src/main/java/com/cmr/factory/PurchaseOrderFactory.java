package com.cmr.factory;

import com.cmr.domain.Item;
import com.cmr.domain.PurchaseOrder;
import com.cmr.support.Utils;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.Date;
@Log4j2
public class PurchaseOrderFactory {

    /**
     * Add private constructor on all static class to prevent it's instantiation
     */
    private PurchaseOrderFactory() {
        log.warn("Attempt to instantiate PurchaseOrderFactory");
        throw new IllegalStateException("PurchaseOrderFactory is a static Utility class and should not be instantiated");
    }
    /**
     * This method create a standard, error free, normal, nominal purchase order. This is meant to be used
     * as a method to create test purchase orders that can be used for success testing or mutated into
     * abhorrent purchase orders for failure testing.
     * @param poNumber the purchase order number as a String
     * @return a complete valid purcharse order entity
     */
    public static PurchaseOrder createTestPOwithRandomNumberOfItems(String poNumber) {
    return (PurchaseOrder.builder()
            .poNumber(poNumber)
            .poDate(new Date(System.currentTimeMillis()))
            .items(createListOfItems(Utils.rnd1To(20)))
            .shipTo(ShipToFactory.createTestShipTo())
            .vendor(VendorFactory.createTestVendor())
            .build());
    
    }
    public static PurchaseOrder createTestPO(String poNumber, int numberOfItems) {
        return (PurchaseOrder.builder()
                .poNumber(poNumber)
                .poDate(new Date(System.currentTimeMillis()))
                .items(createListOfItems(numberOfItems))
                .shipTo(ShipToFactory.createTestShipTo())
                .vendor(VendorFactory.createTestVendor())
                .build());

    }
    private static ArrayList<Item> createListOfItems(int numberOfItemToOrder) {
        ArrayList<Item> itemArrayList = new ArrayList<>();
        for (int i = 0; i < numberOfItemToOrder; i++) {
            Item item = buildRandomItem();
            itemArrayList.add(item);
        }
        return itemArrayList;
    }

    private static  Item buildRandomItem() {
        Item returnItem;
        int answer = Utils.rnd1To(4);

        switch (answer) {
            case 1:
                returnItem = Item.builder()
                        .description("This is a description of an Item. Garmin 430WAAS. I am going to make this description really long, just to see what will happen ")
                        .price(3400.00)
                        .quantity(1)
                        .unit("Each")
                        .build();
                break;
            case 2:
                returnItem = Item.builder()
                        .description("3/4 inch # 8 SS Screws.  ")
                        .price(24.00)
                        .quantity(Utils.rnd1To(8))
                        .unit("25 lot")
                        .build();
                break;
            case 3:
                returnItem = Item.builder()
                        .description("#12 AWG heat shielded wire    ")
                        .price(57.23)
                        .quantity(Utils.rnd1To(5))
                        .unit("50' spool")
                        .build();
                break;
            case 4:
                returnItem = Item.builder()
                        .description("Rebuild kit for KTS 74 - logic board   ")
                        .price(345.87)
                        .quantity(Utils.rnd1To(2))
                        .unit("Each")
                        .build();
                break;
            default:
                returnItem = Item.builder()
                        .description("This is a description of an Item. Garmin 430WAAS ")
                        .price(3400.00)
                        .quantity(Utils.rnd1To(5))
                        .unit("Each")
                        .build();
                break;


        }
        return returnItem;
    }
}
