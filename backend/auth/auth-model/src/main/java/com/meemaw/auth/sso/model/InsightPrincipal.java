package com.meemaw.auth.sso.model;

import com.meemaw.auth.user.model.AuthUser;
import javax.enterprise.context.RequestScoped;
import lombok.Data;
import lombok.experimental.Accessors;

@RequestScoped
@Data
@Accessors(fluent = true)
public class InsightPrincipal {

  private AuthUser user;
}
