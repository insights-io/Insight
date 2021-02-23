package com.rebrowse.auth.permissions;

import static com.rebrowse.auth.user.UserRegistry.S2S_INTERNAL_USER;

import com.rebrowse.auth.user.model.AuthUser;
import com.rebrowse.shared.rest.response.Boom;

public final class AccessManager {

  private AccessManager() {}

  public static void assertCanReadOrganization(AuthUser user, String organizationId) {
    if (!user.getOrganizationId().equals(S2S_INTERNAL_USER.getOrganizationId())
        && !user.getOrganizationId().equals(organizationId)) {
      throw Boom.notFound().exception();
    }
  }
}
