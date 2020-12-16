package com.meemaw.session.sessions.resource.v1;

import com.meemaw.auth.sso.BearerTokenSecurityScheme;
import com.meemaw.auth.sso.SsoSessionCookieSecurityScheme;
import com.meemaw.session.model.CreatePageVisitDTO;
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

@Path(PageVisitResource.PATH)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RegisterRestClient(configKey = "session-api")
public interface PageVisitResource {

  String PATH = "/v1/pages";
  String TAG = "Page Visit";

  @POST
  @Tag(name = TAG)
  @Operation(summary = "Create page visit")
  CompletionStage<Response> create(
      @NotNull(message = "Required") @Valid CreatePageVisitDTO body,
      @NotBlank(message = "Required") @HeaderParam(HttpHeaders.USER_AGENT) String userAgent);

  @GET
  @Path("{id}")
  @SecurityRequirements(
      value = {
        @SecurityRequirement(name = BearerTokenSecurityScheme.NAME),
        @SecurityRequirement(name = SsoSessionCookieSecurityScheme.NAME)
      })
  @Tag(name = TAG)
  @Operation(summary = "Retrieve page visit")
  CompletionStage<Response> retrieve(
      @PathParam("id") UUID id, @QueryParam("organizationId") String organizationId);

  @GET
  @Path("count")
  @Tag(name = TAG)
  @Operation(summary = "Count page visits")
  @SecurityRequirements(
      value = {
        @SecurityRequirement(name = BearerTokenSecurityScheme.NAME),
        @SecurityRequirement(name = SsoSessionCookieSecurityScheme.NAME)
      })
  CompletionStage<Response> count();
}
