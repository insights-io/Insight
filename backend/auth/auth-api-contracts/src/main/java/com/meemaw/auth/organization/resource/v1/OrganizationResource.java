package com.meemaw.auth.organization.resource.v1;

import com.meemaw.auth.sso.cookie.CookieAuth;
import java.util.concurrent.CompletionStage;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path(OrganizationResource.PATH)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RegisterRestClient(configKey = "auth-api")
public interface OrganizationResource {

  String PATH = "/v1/organizations";

  @GET
  @Path("members")
  @CookieAuth
  CompletionStage<Response> members();

  @GET
  @CookieAuth
  CompletionStage<Response> organization();

  // TODO: S2S authentication
  @GET
  @Path("{organizationId}")
  CompletionStage<Response> organization(@PathParam("organizationId") String organizationId);
}
