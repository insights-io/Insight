package com.meemaw.session.location.resource;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import com.meemaw.session.location.model.dto.IpStackLocationDTO;

import javax.ws.rs.*;

@RegisterRestClient(configKey = "location-lookup-resource")
public interface LocationLookupResource {

  @GET
  @Path("/{ip}")
  @Produces("application/json")
  IpStackLocationDTO lookupByIp(
      @PathParam("ip") String ip, @QueryParam("access_key") String accessKey);
}
