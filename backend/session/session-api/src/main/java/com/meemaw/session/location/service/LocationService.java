package com.meemaw.session.location.service;

import com.meemaw.location.model.Located;

public interface LocationService {

  Located lookupByIp(String ip);
}
