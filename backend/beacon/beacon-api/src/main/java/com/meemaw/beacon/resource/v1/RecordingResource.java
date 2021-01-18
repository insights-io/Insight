package com.meemaw.beacon.resource.v1;

import java.util.UUID;
import java.util.concurrent.CompletionStage;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path(RecordingResource.PATH)
public interface RecordingResource {

  String PATH = "/v1/recording";

  @POST
  @Consumes({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON})
  @Path("/beat")
  CompletionStage<Response> beat(
      @NotBlank(message = "Required")
          @Size(min = 6, max = 6, message = "Has to be " + 6 + " characters long")
          @QueryParam("organizationId")
          String organizationId,
      @NotNull(message = "Required") @QueryParam("sessionId") UUID sessionId,
      @NotNull(message = "Required") @QueryParam("deviceId") UUID deviceId,
      @NotNull(message = "Required") @QueryParam("pageVisitId") UUID pageVisitId,
      String body);
}
