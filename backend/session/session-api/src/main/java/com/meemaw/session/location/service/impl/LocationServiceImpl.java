package com.meemaw.session.location.service.impl;

import com.meemaw.location.model.Location;
import com.meemaw.location.model.dto.LocationDTO;
import com.meemaw.session.location.resource.LocationLookupResource;
import com.meemaw.session.location.resource.WhatIsMyIpResource;
import com.meemaw.session.location.service.LocationService;
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
@SuppressWarnings("PMD.AvoidUsingHardCodedIP")
public class LocationServiceImpl implements LocationService {

  private static final String LOCALHOST_IPV4 = "127.0.0.1";
  private static final String LOCALHOST_IPV6 = "0:0:0:0:0:0:0:1";

  private static final Map<String, Location> CACHE = new HashMap<>();
  private static final Optional<String> ACCESS_KEY =
      Optional.ofNullable(System.getenv("LOCATION_LOOKUP_SERVICE_ACCESS_KEY"));

  @Inject @RestClient LocationLookupResource locationLookupResource;
  @Inject @RestClient WhatIsMyIpResource whatIsMyIpResource;

  @Traced
  private Location lookupByIpRemotely(String ip) {
    if (ACCESS_KEY.isEmpty()) {
      log.warn("[LOCATION]: Access key not configured ... skipping lookup for IP: {}", ip);
      return LocationDTO.builder().ip(ip).build();
    }

    // TODO: better check to determine local addresses
    if (ip.equals(LOCALHOST_IPV4) || ip.equals(LOCALHOST_IPV6)) {
      ip = whatIsMyIpResource.get();
      log.info("[LOCATION]: Resolved localhost to public IP: {}", ip);
    }

    log.info("[LOCATION]: Looking up location for IP: {}", ip);
    return locationLookupResource.lookupByIp(ip, ACCESS_KEY.get()).toInternalRepresentation();
  }

  @Override
  public Location lookupByIp(String ip) {
    return CACHE.computeIfAbsent(ip, this::lookupByIpRemotely);
  }
}
