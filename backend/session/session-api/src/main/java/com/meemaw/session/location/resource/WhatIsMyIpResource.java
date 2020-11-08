package com.meemaw.session.location.resource;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.GET;

@RegisterRestClient(configKey = "what-is-my-ip-resource")
public interface WhatIsMyIpResource {

  @GET
  String get();
}
