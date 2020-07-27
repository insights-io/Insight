package com.meemaw.location.model;

public interface Location {

  String getIp();

  String getCountryName();

  String getRegionName();

  String getCity();

  String getZip();

  double getLatitude();

  double getLongitude();
}
