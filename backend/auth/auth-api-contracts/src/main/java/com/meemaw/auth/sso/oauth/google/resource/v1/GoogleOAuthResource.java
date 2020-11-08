package com.meemaw.auth.sso.oauth.google.resource.v1;

import com.meemaw.auth.sso.oauth.OAuthResource;

import javax.ws.rs.Path;

@Path(GoogleOAuthResource.PATH)
public interface GoogleOAuthResource extends OAuthResource {

  String PATH = OAuthResource.PATH + "/google";
}
