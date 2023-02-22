package com.cmr.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Vendor {
    private String name;
    private String address;
    private String city;
    private String state;
    private String zip;
    private String country;
    private String phone;
    private String fax;
    private String email;


}
