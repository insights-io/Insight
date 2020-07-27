package com.meemaw.location.model.dto;

import com.meemaw.location.model.Location;
import lombok.Builder;
import lombok.Value;

@Value
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
