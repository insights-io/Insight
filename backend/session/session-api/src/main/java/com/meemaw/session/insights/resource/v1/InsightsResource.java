package com.meemaw.session.insights.resource.v1;

import com.meemaw.auth.sso.cookie.CookieAuth;
import java.util.concurrent.CompletionStage;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path(InsightsResource.PATH)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface InsightsResource {

  String PATH = "/v1/sessions/insights";

  @GET
  @Path("count")
  @CookieAuth
  CompletionStage<Response> count();
}
