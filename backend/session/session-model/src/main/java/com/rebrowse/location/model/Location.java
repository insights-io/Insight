package com.rebrowse.location.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
@Builder
public class Location implements Located {

  String ip;
  String countryName;
  String regionName;
  String city;
  String zip;
  String continentName;
  double latitude;
  double longitude;
}
