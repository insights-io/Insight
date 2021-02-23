package com.rebrowse.auth.core.config.resource;

import com.rebrowse.auth.core.config.model.AppConfig;
import com.rebrowse.shared.config.resource.AbstractAppConfigResource;
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
