package com.meemaw.session.sessions.resource.v1;

import com.meemaw.auth.sso.BearerTokenSecurityScheme;
import com.meemaw.auth.sso.SessionCookieSecurityScheme;
import com.meemaw.session.model.CreatePageDTO;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirements;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path(SessionPageResource.PATH)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RegisterRestClient(configKey = "session-api")
public interface SessionPageResource {

  String PATH = SessionResource.PATH;
  String TAG = "Session Page";

  @POST
  @Tag(name = TAG)
  @Operation(summary = "Create page")
  CompletionStage<Response> create(
      @NotNull(message = "Required") @Valid CreatePageDTO body,
      @NotBlank(message = "Required") @HeaderParam(HttpHeaders.USER_AGENT) String userAgent);

  @GET
  @Path("{sessionId}/pages/{pageId}")
  @SecurityRequirements(
      value = {
        @SecurityRequirement(name = BearerTokenSecurityScheme.NAME),
        @SecurityRequirement(name = SessionCookieSecurityScheme.NAME)
      })
  @Tag(name = TAG)
  @Operation(summary = "Retrieve page")
  CompletionStage<Response> retrieve(
      @PathParam("sessionId") UUID sessionId,
      @PathParam("pageId") UUID pageId,
      @QueryParam("organizationId") String organizationId);
}
