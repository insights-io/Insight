package com.meemaw.auth.sso.token.resource.v1;

import com.meemaw.auth.sso.bearer.BearerTokenAuth;
import com.meemaw.auth.sso.cookie.CookieAuth;
import com.meemaw.auth.sso.session.resource.v1.SsoResource;
import java.util.concurrent.CompletionStage;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path(AuthTokenResource.PATH)
@Produces(MediaType.APPLICATION_JSON)
@RegisterRestClient(configKey = "auth-api")
public interface AuthTokenResource {

  String PATH = SsoResource.PATH + "/auth/token";
  String TAG = "Auth Token";

  @GET
  @Path("user")
  @BearerTokenAuth
  @Tag(name = TAG)
  @Operation(summary = "Retrieve authenticated user")
  CompletionStage<Response> me(@HeaderParam(HttpHeaders.AUTHORIZATION) String bearerToken);

  @GET
  @CookieAuth
  @Tag(name = TAG)
  @Operation(summary = "List auth tokens")
  CompletionStage<Response> list();

  @POST
  @CookieAuth
  @Tag(name = TAG)
  @Operation(summary = "List auth token")
  CompletionStage<Response> create();

  @DELETE
  @CookieAuth
  @Path("{token}")
  @Tag(name = TAG)
  @Operation(summary = "Delete auth token")
  CompletionStage<Response> delete(@PathParam("token") String token);
}
