package com.meemaw.session.resource.v1;

import com.meemaw.auth.sso.BearerTokenSecurityScheme;
import com.meemaw.auth.sso.SessionCookieSecurityScheme;
import com.meemaw.session.model.CreatePageDTO;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirements;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path(SessionResource.PATH)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RegisterRestClient(configKey = "session-resource")
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

  @POST
  @Operation(summary = "Create page")
  CompletionStage<Response> createPage(
      @NotNull(message = "Required") @Valid CreatePageDTO body,
      @NotBlank(message = "Required") @HeaderParam(HttpHeaders.USER_AGENT) String userAgent);

  @GET
  @Path("{sessionId}/pages/{pageId}")
  @SecurityRequirements(
      value = {
        @SecurityRequirement(name = BearerTokenSecurityScheme.NAME),
        @SecurityRequirement(name = SessionCookieSecurityScheme.NAME)
      })
  @Operation(summary = "Retrieve page")
  CompletionStage<Response> retrievePage(
      @PathParam("sessionId") UUID sessionId,
      @PathParam("pageId") UUID pageId,
      @QueryParam("organizationId") String organizationId,
      @HeaderParam(HttpHeaders.AUTHORIZATION) String authorization);
}
