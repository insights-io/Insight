package com.meemaw.auth.core.config.resource;

import com.meemaw.auth.core.config.model.AppConfig;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path(ConfigResource.PATH)
@Produces(MediaType.APPLICATION_JSON)
public class ConfigResource {

  public static final String PATH = "/v1/config";

  @Inject AppConfig appConfig;

  @GET
  public AppConfig get() {
    return appConfig;
  }
}
