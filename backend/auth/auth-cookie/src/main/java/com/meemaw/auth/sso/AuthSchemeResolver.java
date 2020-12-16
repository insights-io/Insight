package com.meemaw.auth.sso;

import javax.ws.rs.container.ContainerRequestContext;

public interface AuthSchemeResolver {

  void tryAuthenticate(ContainerRequestContext context);

  AuthScheme getAuthScheme();
}
