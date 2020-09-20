package com.meemaw.auth.sso.oauth.microsoft.resource.v1;

import com.meemaw.auth.sso.oauth.OAuth2Resource;
import javax.ws.rs.Path;

@Path(OAuth2MicrosoftResource.PATH)
public interface OAuth2MicrosoftResource extends OAuth2Resource {

  String PATH = OAuth2Resource.PATH + "/microsoft";
}
