package com.meemaw.session.resource.v1;

import com.meemaw.session.model.PageDTO;
import com.meemaw.shared.auth.CookieAuth;
import java.util.concurrent.CompletionStage;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path(SessionResource.PATH)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface SessionResource {

  String PATH = "/v1/sessions";

  @POST
  CompletionStage<Response> page(@NotNull(message = "Payload is required") @Valid PageDTO payload);

  @GET
  @CookieAuth
  CompletionStage<Response> count();

}
