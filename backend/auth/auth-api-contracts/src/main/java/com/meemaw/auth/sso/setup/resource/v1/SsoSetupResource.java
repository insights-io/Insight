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
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path(SsoSetupResource.PATH)
@Produces(MediaType.APPLICATION_JSON)
public interface SsoSetupResource {

  String PATH = SsoResource.PATH + "/setup";
  String TAG = "SSO";

  @POST
  @CookieAuth
  @Tag(name = TAG)
  @Operation(summary = "Create SSO setup")
  CompletionStage<Response> create(@NotNull(message = "Required") @Valid CreateSsoSetupDTO body);

  @GET
  @CookieAuth
  @Tag(name = TAG)
  @Operation(summary = "Create SSO setup associated with authenticated user")
  CompletionStage<Response> get();

  @GET
  @Path("{domain}")
  @Tag(name = TAG)
  @Operation(summary = "Create SSO setup associated with a domain")
  CompletionStage<Response> get(@PathParam("domain") String domain);
}
