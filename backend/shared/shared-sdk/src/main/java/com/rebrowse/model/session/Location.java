package com.rebrowse.model.session;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class Location {

  String ip;
  String countryName;
  String regionName;
  String city;
  String zip;
  String continentName;
  double latitude;
  double longitude;
}
