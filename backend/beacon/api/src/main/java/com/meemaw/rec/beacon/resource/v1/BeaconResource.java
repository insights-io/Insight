package com.meemaw.rec.beacon.resource.v1;

import java.util.UUID;
import java.util.concurrent.CompletionStage;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path(BeaconResource.PATH)
public interface BeaconResource {

  String PATH = "/v1/beacon";

  @POST
  @Consumes({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON})
  @Path("/beat")
  CompletionStage<Response> textBeacon(
      @QueryParam("SessionID") UUID sessionID,
      @QueryParam("UserID") UUID userID,
      @QueryParam("PageID") UUID pageID,
      String payload);

}
