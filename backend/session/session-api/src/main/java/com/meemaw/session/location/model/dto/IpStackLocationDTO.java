package com.meemaw.session.location.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.meemaw.location.model.Located;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public class IpStackLocationDTO implements Located {

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
}
