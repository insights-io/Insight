package com.meemaw.session.location.service;

import com.meemaw.location.model.LocationDTO;

public interface LocationService {

  /**
   * Lookup location by IP address.
   *
   * @param ip address associated with the request
   * @return detailed location
   */
  LocationDTO lookupByIp(String ip);
}
