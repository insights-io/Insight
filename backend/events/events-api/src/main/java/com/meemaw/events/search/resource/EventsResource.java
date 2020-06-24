package com.meemaw.events.search.resource;

import com.meemaw.auth.sso.cookie.CookieAuth;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path(EventsResource.PATH)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface EventsResource {

  String PATH = "/v1/sessions";

  @GET
  @Path("{sessionId}/events/search")
  @CookieAuth
  CompletionStage<Response> search(@PathParam("sessionId") UUID sessionId);
}
