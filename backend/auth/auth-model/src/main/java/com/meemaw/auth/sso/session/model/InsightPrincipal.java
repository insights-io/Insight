package com.meemaw.auth.sso.session.model;

import lombok.Data;
import lombok.experimental.Accessors;

import com.meemaw.auth.user.model.AuthUser;

import javax.enterprise.context.RequestScoped;

@RequestScoped
@Data
@Accessors(fluent = true)
public class InsightPrincipal {

  private AuthUser user;
  private String apiKey;
  private String sessionId;
}
