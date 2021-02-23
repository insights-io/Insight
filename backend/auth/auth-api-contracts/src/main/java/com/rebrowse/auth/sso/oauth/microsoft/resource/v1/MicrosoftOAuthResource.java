package com.rebrowse.auth.sso.oauth.microsoft.resource.v1;

import com.rebrowse.auth.sso.oauth.OAuthResource;
import javax.ws.rs.Path;

@Path(MicrosoftOAuthResource.PATH)
public interface MicrosoftOAuthResource extends OAuthResource {

  String PATH = OAuthResource.PATH + "/microsoft";
}
