package com.meemaw.session.resource.v1;

import com.meemaw.auth.organization.model.validation.OrganizationId;
import com.meemaw.auth.sso.cookie.CookieAuth;
import com.meemaw.session.model.CreatePageDTO;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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
  CompletionStage<Response> page(
      @NotNull(message = "Payload is required") @Valid CreatePageDTO payload);

  @GET
  @CookieAuth
  CompletionStage<Response> count();

  // TODO: this should be authenticated
  @GET
  @Path("{SessionID}/pages/{PageID}")
  CompletionStage<Response> get(
      @PathParam("SessionID") UUID sessionID,
      @PathParam("PageID") UUID pageID,
      @OrganizationId @QueryParam("orgID") String orgID);

  // TODO: this should be authenticated
  @GET
  @Path("search")
  CompletionStage<Response> search();
}
