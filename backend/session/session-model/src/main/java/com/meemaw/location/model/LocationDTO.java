package com.meemaw.location.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public class LocationDTO {

  String ip;

  @JsonProperty("country_name")
  String countryName;

  @JsonProperty("region_name")
  String regionName;

  String city;

  String zip;
  double latitude;
  double longitude;
}
