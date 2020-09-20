package com.meemaw.auth.sso.setup.resource.v1;

import com.meemaw.auth.sso.cookie.CookieAuth;
import com.meemaw.auth.sso.session.resource.v1.SsoResource;
import com.meemaw.auth.sso.setup.model.dto.CreateSsoSetupDTO;
import java.util.concurrent.CompletionStage;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path(SsoSetupResource.PATH)
@Produces(MediaType.APPLICATION_JSON)
public interface SsoSetupResource {

  String PATH = SsoResource.PATH + "/setup";

  @POST
  @CookieAuth
  CompletionStage<Response> setup(@NotNull(message = "Required") @Valid CreateSsoSetupDTO body);

  @GET
  @CookieAuth
  CompletionStage<Response> get();

  @GET
  @Path("{domain}")
  CompletionStage<Response> get(@PathParam("domain") String domain);
}
