package com.meemaw.session.location.service.impl;

import com.meemaw.location.model.Location;
import com.meemaw.location.model.dto.LocationDTO;
import com.meemaw.session.location.resource.LocationLookupResource;
import com.meemaw.session.location.resource.WhatIsMyIpResource;
import com.meemaw.session.location.service.LocationService;
import com.meemaw.shared.ip.IpUtils;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.opentracing.Traced;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
@Slf4j
@SuppressWarnings("PMD.AvoidUsingHardCodedIP")
public class LocationServiceImpl implements LocationService {

  private static final Map<String, Location> CACHE = new HashMap<>();
  private static final String ACCESS_KEY = System.getenv("LOCATION_LOOKUP_SERVICE_ACCESS_KEY");

  @Inject @RestClient LocationLookupResource locationLookupResource;
  @Inject @RestClient WhatIsMyIpResource whatIsMyIpResource;

  @Traced
  private Location lookupByIpRemotely(String ip) {
    if (ACCESS_KEY == null) {
      log.warn("[LOCATION]: Access key not configured ... skipping lookup for IP={}", ip);
      return LocationDTO.builder().ip(ip).build();
    }

    try {
      if (IpUtils.isLocalAddress(ip)) {
        ip = whatIsMyIpResource.get();
        log.info("[LOCATION]: Resolved localhost to public IP={}", ip);
      }
    } catch (UnknownHostException ex) {
      log.error("[LOCATION]: Failed to check if local address IP={}", ip, ex);
    }

    log.info("[LOCATION]: Looking up location for IP={}", ip);
    return locationLookupResource.lookupByIp(ip, ACCESS_KEY).dto();
  }

  @Override
  public Location lookupByIp(String ip) {
    return CACHE.computeIfAbsent(ip, this::lookupByIpRemotely);
  }
}
