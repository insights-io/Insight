package com.meemaw.session.location.service;

import com.meemaw.location.model.Location;

public interface LocationService {

  Location lookupByIp(String ip);
}
