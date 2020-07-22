package com.meemaw.session.location.service;

import com.meemaw.location.model.LocationDTO;
import com.meemaw.session.location.resource.LocationLookupResource;
import com.meemaw.session.location.resource.WhatIsMyIpResource;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.opentracing.Traced;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
@Slf4j
public class LocationService {

  private static final String LOCALHOST = "127.0.0.1";
  private static final Map<String, LocationDTO> CACHE = new HashMap<>();
  private static final Optional<String> ACCESS_KEY =
      Optional.ofNullable(System.getenv("LOCATION_LOOKUP_SERVICE_ACCESS_KEY"));

  @Inject @RestClient LocationLookupResource locationLookupResource;
  @Inject @RestClient WhatIsMyIpResource whatIsMyIpResource;

  @Traced
  private LocationDTO lookupByIpRemotely(String ip) {
    if (ACCESS_KEY.isEmpty()) {
      log.warn("[LOCATION]: Access key not configured ... skipping lookup for IP: {}", ip);
      return LocationDTO.builder().ip(ip).build();
    }

    if (ip.equals(LOCALHOST)) {
      String publicIp = whatIsMyIpResource.get();
      log.info("[LOCATION]: Resolved localhost to public IP: {}", publicIp);
      return locationLookupResource.lookupByIp(publicIp, ACCESS_KEY.get());
    }

    log.info("[LOCATION]: Looking up location for IP: {}", ip);
    return locationLookupResource.lookupByIp(ip, ACCESS_KEY.get());
  }

  public LocationDTO lookupByIp(String ip) {
    return CACHE.computeIfAbsent(ip, this::lookupByIpRemotely);
  }
}
