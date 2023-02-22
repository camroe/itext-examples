package com.cmr.factory;

import com.cmr.domain.Vendor;

public class VendorFactory {
    public static Vendor createVendor(String name, String address, String city, String state, String zip, String country, String phone, String fax, String email) {
        return Vendor.builder()
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

    public static Vendor createTestVendor() {
        return Vendor.builder()
                .name("Test Vendor")
                .address("123 Test Street")
                .city("Test City")
                .state("Test State")
                .zip("12345")
                .country("Test Country")
                .phone("123-456-7890")
                .fax("123-456-7890")
                .email("TestVendor@example.com")
                .build();

    }
}
