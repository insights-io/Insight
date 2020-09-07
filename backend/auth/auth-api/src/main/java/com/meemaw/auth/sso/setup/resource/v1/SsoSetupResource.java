package com.meemaw.auth.sso.setup.resource.v1;

import com.meemaw.auth.sso.cookie.CookieAuth;
import com.meemaw.auth.sso.resource.v1.SsoResource;
import java.util.concurrent.CompletionStage;
import javax.validation.constraints.NotBlank;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path(SsoSetupResource.PATH)
public interface SsoSetupResource {

  String PATH = SsoResource.PATH + "/setup";

  @POST
  @CookieAuth
  CompletionStage<Response> setup(
      @NotBlank(message = "Required") @FormParam("configurationEndpoint")
          String configurationEndpoint);
}
