package com.meemaw.location.model.dto;

import lombok.*;

import com.meemaw.location.model.Location;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
@Builder
public class LocationDTO implements Location {

  String ip;
  String countryName;
  String regionName;
  String city;
  String zip;
  String continentName;
  double latitude;
  double longitude;
}
