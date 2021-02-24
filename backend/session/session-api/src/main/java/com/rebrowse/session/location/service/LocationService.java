package com.rebrowse.session.location.service;

import com.rebrowse.location.model.Located;

public interface LocationService {

  Located lookupByIp(String ip);
}
