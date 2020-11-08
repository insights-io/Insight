package com.meemaw.session.location.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.meemaw.location.model.Location;
import com.meemaw.location.model.dto.LocationDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public class IpStackLocationDTO implements Location {

  String ip;
  String zip;
  double latitude;
  double longitude;
  String city;

  @JsonProperty("continent_name")
  String continentName;

  @JsonProperty("country_name")
  String countryName;

  @JsonProperty("region_name")
  String regionName;

  @JsonIgnore
  public Location toInternalRepresentation() {
    return LocationDTO.builder()
        .ip(ip)
        .zip(zip)
        .latitude(latitude)
        .longitude(longitude)
        .city(city)
        .countryName(countryName)
        .regionName(regionName)
        .continentName(continentName)
        .build();
  }
}
