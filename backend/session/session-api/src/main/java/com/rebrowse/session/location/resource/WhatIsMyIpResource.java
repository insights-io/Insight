package com.rebrowse.session.location.resource;

import javax.ws.rs.GET;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "what-is-my-ip-resource")
public interface WhatIsMyIpResource {

  @GET
  String get();
}
