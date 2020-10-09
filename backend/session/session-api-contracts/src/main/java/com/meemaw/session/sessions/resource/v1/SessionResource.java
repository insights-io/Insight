package com.meemaw.session.sessions.resource.v1;

import com.meemaw.auth.sso.BearerTokenSecurityScheme;
import com.meemaw.auth.sso.SessionCookieSecurityScheme;
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

@Path(SessionResource.PATH)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface SessionResource {

  String PATH = "/v1/sessions";
  String TAG = "Session";

  @GET
  @SecurityRequirements(
      value = {
        @SecurityRequirement(name = BearerTokenSecurityScheme.NAME),
        @SecurityRequirement(name = SessionCookieSecurityScheme.NAME)
      })
  @Tag(name = TAG)
  @Operation(summary = "List sessions")
  CompletionStage<Response> list();

  @GET
  @Path("{sessionId}")
  @SecurityRequirements(
      value = {
        @SecurityRequirement(name = BearerTokenSecurityScheme.NAME),
        @SecurityRequirement(name = SessionCookieSecurityScheme.NAME)
      })
  @Tag(name = TAG)
  @Operation(summary = "Retrieve session")
  CompletionStage<Response> retrieve(@PathParam("sessionId") UUID sessionId);
}
