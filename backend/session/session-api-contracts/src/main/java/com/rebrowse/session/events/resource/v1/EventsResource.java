package com.rebrowse.session.events.resource.v1;

import com.rebrowse.auth.sso.BearerTokenSecurityScheme;
import com.rebrowse.auth.sso.SsoSessionCookieSecurityScheme;
import com.rebrowse.session.sessions.resource.v1.SessionResource;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirements;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path(EventsResource.PATH)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface EventsResource {

  String PATH = SessionResource.PATH;
  String TAG = "Event";

  @GET
  @Path("{sessionId}/events/search")
  @SecurityRequirements(
      value = {
        @SecurityRequirement(name = BearerTokenSecurityScheme.NAME),
        @SecurityRequirement(name = SsoSessionCookieSecurityScheme.NAME)
      })
  @Tag(name = TAG)
  @Operation(summary = "Search for events")
  CompletionStage<Response> search(@PathParam("sessionId") UUID sessionId);
}
