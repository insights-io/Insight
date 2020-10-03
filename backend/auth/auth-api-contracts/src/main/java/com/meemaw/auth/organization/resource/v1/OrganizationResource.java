package com.meemaw.auth.organization.resource.v1;

import com.meemaw.auth.sso.bearer.BearerTokenAuth;
import com.meemaw.auth.sso.cookie.CookieAuth;
import java.util.concurrent.CompletionStage;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path(OrganizationResource.PATH)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RegisterRestClient(configKey = "auth-api")
public interface OrganizationResource {

  String PATH = "/v1/organizations";
  String TAG = "Organization";

  @GET
  @Path("members")
  @CookieAuth
  @Tag(name = TAG)
  CompletionStage<Response> members();

  @GET
  @CookieAuth
  @Tag(name = TAG)
  CompletionStage<Response> organization();

  @GET
  @Path("{id}")
  @BearerTokenAuth
  @Tag(name = TAG)
  default CompletionStage<Response> organization(
      @PathParam("id") String id, @HeaderParam(HttpHeaders.AUTHORIZATION) String authorization) {
    return organization(id);
  }

  CompletionStage<Response> organization(String organizationId);
}
