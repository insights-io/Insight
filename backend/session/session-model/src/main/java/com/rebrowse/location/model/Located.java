package com.rebrowse.location.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface Located {

  String getIp();

  String getCountryName();

  String getRegionName();

  String getContinentName();

  String getCity();

  String getZip();

  double getLatitude();

  double getLongitude();

  @JsonIgnore
  default Location location() {
    return Location.builder()
        .ip(getIp())
        .zip(getZip())
        .latitude(getLatitude())
        .longitude(getLongitude())
        .city(getCity())
        .countryName(getCountryName())
        .regionName(getRegionName())
        .continentName(getContinentName())
        .build();
  }

  @JsonIgnore
  default LocationMapper mapper() {
    return LocationMapper.builder()
        .ip(getIp())
        .zip(getZip())
        .latitude(getLatitude())
        .longitude(getLongitude())
        .city(getCity())
        .countryName(getCountryName())
        .regionName(getRegionName())
        .continentName(getContinentName())
        .build();
  }
}
