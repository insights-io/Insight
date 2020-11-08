package com.meemaw.auth.sso.session.model;

import lombok.Value;

import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.auth.user.model.UserRole;

import java.security.Principal;
import javax.ws.rs.core.SecurityContext;

@Value
public class InsightSecurityContext implements SecurityContext {

  Principal userPrincipal;
  UserRole userRole;
  boolean secure;

  public InsightSecurityContext(AuthUser ssoUser, boolean isSecure) {
    this.userRole = ssoUser.getRole();
    this.userPrincipal = () -> ssoUser.getId().toString();
    this.secure = isSecure;
  }

  @Override
  public boolean isUserInRole(String role) {
    return UserRole.fromString(role).equals(userRole);
  }

  @Override
  public String getAuthenticationScheme() {
    return "Cookie-Based-Auth-Scheme";
  }
}
