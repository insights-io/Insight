package com.meemaw.session.insights.resource.v1;

import com.meemaw.auth.sso.BearerTokenSecurityScheme;
import com.meemaw.auth.sso.SessionCookieSecurityScheme;
import java.util.List;
import java.util.concurrent.CompletionStage;
import javax.validation.constraints.NotEmpty;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirements;

@Path(InsightsResource.PATH)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface InsightsResource {

  String PATH = "/v1/sessions/insights";
  String ON = "on";

  @GET
  @Path("count")
  @SecurityRequirements(
      value = {
        @SecurityRequirement(name = BearerTokenSecurityScheme.NAME),
        @SecurityRequirement(name = SessionCookieSecurityScheme.NAME)
      })
  CompletionStage<Response> count();

  @GET
  @Path("distinct")
  @SecurityRequirements(
      value = {
        @SecurityRequirement(name = BearerTokenSecurityScheme.NAME),
        @SecurityRequirement(name = SessionCookieSecurityScheme.NAME)
      })
  CompletionStage<Response> distinct(
      @NotEmpty(message = "Required") @QueryParam(ON) List<String> on);
}
