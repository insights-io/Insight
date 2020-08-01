package com.meemaw.location.model.dto;

import com.meemaw.location.model.Location;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;

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
  double latitude;
  double longitude;
}
