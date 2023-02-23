package com.cmr.factory;

import com.cmr.domain.ShipTo;

public class ShipToFactory {
    public static ShipTo createShipTo(String name, String address, String city, String state, String zip, String country, String phone, String fax, String email) {
        return ShipTo.builder()
                .name(name)
                .address(address)
                .city(city)
                .state(state)
                .zip(zip)
                .country(country)
                .phone(phone)
                .fax(fax)
                .email(email)
                .build();
    }

    public static ShipTo createTestShipTo() {
        return ShipTo.builder()
                .name("Test ShipTo")
                .address("123  Very Long Big Ass Stupid Test Street That could cause weirdness")
                .city("Test City")
                .state("Test State")
                .zip("12345")
                .country("Test Country")
                .phone("123-456-7890")
                .fax("123-456-7890")
                .email("testShipTo@example.com")
                .build();
    }
}
