package com.meemaw.session.resource.v1;

import com.meemaw.auth.sso.cookie.CookieAuth;
import com.meemaw.session.model.CreatePageDTO;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path(SessionResource.PATH)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RegisterRestClient(configKey = "session-resource")
public interface SessionResource {

  String PATH = "/v1/sessions";

  @POST
  CompletionStage<Response> createPage(
      @NotNull(message = "Required") @Valid CreatePageDTO body,
      @HeaderParam(HttpHeaders.USER_AGENT) String userAgent,
      @HeaderParam("X-Forwarded-For") String xForwardedFor);

  @GET
  @CookieAuth
  CompletionStage<Response> getSessions();

  @GET
  @Path("{sessionId}")
  @CookieAuth
  CompletionStage<Response> getSession(@PathParam("sessionId") UUID sessionId);

  @GET
  @Path("{sessionId}/pages/{pageId}")
  @CookieAuth
  CompletionStage<Response> getPage(
      @PathParam("sessionId") UUID sessionId, @PathParam("pageId") UUID pageId);

  @GET
  @Path("search")
  @CookieAuth
  CompletionStage<Response> searchSessions();
}
