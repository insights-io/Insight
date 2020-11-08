package com.meemaw.auth.core.config.resource;

import lombok.Getter;

import com.meemaw.auth.core.config.model.AppConfig;
import com.meemaw.shared.config.resource.AbstractAppConfigResource;

import javax.inject.Inject;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path(AppConfigResource.PATH)
@Produces(MediaType.APPLICATION_JSON)
public class AppConfigResource extends AbstractAppConfigResource<AppConfig> {

  @Getter @Inject AppConfig appConfig;
}
