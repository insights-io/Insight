package com.meemaw.session.events.resource.v1;

import com.meemaw.auth.sso.SessionCookieSecurityScheme;
import com.meemaw.session.sessions.v1.SessionResource;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirements;

@Path(SessionResource.PATH)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface EventsResource {

  @GET
  @Path("{sessionId}/events/search")
  @SecurityRequirements(value = {@SecurityRequirement(name = SessionCookieSecurityScheme.NAME)})
  CompletionStage<Response> search(@PathParam("sessionId") UUID sessionId);
}
