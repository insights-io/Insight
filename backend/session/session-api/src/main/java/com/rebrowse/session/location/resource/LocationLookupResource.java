package com.rebrowse.session.location.resource;

import com.rebrowse.session.location.model.dto.IpStackLocationDTO;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "location-lookup-resource")
public interface LocationLookupResource {

  @GET
  @Path("/{ip}")
  @Produces("application/json")
  IpStackLocationDTO lookupByIp(
      @PathParam("ip") String ip, @QueryParam("access_key") String accessKey);
}
