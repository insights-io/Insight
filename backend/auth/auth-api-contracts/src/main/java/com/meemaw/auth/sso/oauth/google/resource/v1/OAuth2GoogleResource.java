package com.meemaw.auth.sso.oauth.google.resource.v1;

import com.meemaw.auth.sso.oauth.OAuth2Resource;
import javax.ws.rs.Path;

@Path(OAuth2GoogleResource.PATH)
public interface OAuth2GoogleResource extends OAuth2Resource {

  String PATH = OAuth2Resource.PATH + "/google";
}
