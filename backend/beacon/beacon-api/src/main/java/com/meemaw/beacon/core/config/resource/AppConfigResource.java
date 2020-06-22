package com.meemaw.beacon.core.config.resource;

import com.meemaw.beacon.core.config.model.AppConfig;
import com.meemaw.shared.config.resource.AbstractAppConfigResource;
import javax.inject.Inject;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import lombok.Getter;

@Path(AppConfigResource.PATH)
@Produces(MediaType.APPLICATION_JSON)
public class AppConfigResource extends AbstractAppConfigResource<AppConfig> {

  @Getter @Inject AppConfig appConfig;
}
