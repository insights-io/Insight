package com.meemaw.auth.sso.saml.resource.v1;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;

public class SamlResourceImpl implements SamlResource {

  @Context HttpServletResponse response;
  @Context HttpServletRequest request;

  @Override
  public Response signIn(String destination) {
    String clientId = "N0fqSvC6QBRRiAU01SuM7XVW609kF78S";
    UriBuilder builder =
        UriBuilder.fromUri("https://insights-io.eu.auth0.com/samlp")
            .path(clientId)
            .queryParam("connection", "Username-Password-Authentication")
            .queryParam("RelayState", "Hello world");

    return Response.status(Status.FOUND).header("Location", builder.build()).build();
  }

  @Override
  public CompletionStage<Response> callback(
      String samlResponse, String relayState, String sessionRelayState) {

    System.out.println(samlResponse);
    System.out.println(relayState);
    return CompletableFuture.completedStage(Response.ok().build());
  }
}
