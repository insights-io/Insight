package com.meemaw.auth.user.resource.v1;

import com.meemaw.auth.sso.cookie.CookieAuth;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.PATCH;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path(UserResource.PATH)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface UserResource {

  String PATH = "/v1/user";

  @PATCH
  @CookieAuth
  CompletionStage<Response> update(@NotNull(message = "Required") Map<String, ?> body);
}
