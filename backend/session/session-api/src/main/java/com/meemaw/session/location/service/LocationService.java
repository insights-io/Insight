package com.meemaw.session.location.service;

import com.meemaw.location.model.LocationDTO;

public interface LocationService {

  LocationDTO lookupByIp(String ip);
}
