package com.rebrowse.auth.user;

import com.rebrowse.auth.user.model.AuthUser;
import com.rebrowse.auth.user.model.UserRole;
import com.rebrowse.auth.user.model.dto.UserDTO;
import com.rebrowse.shared.SharedConstants;
import com.rebrowse.api.RebrowseApi;
import java.time.OffsetDateTime;
import java.util.UUID;

public final class UserRegistry {

  public static final AuthUser S2S_INTERNAL_USER =
      new UserDTO(
          UUID.randomUUID(),
          String.format("internal-s2s@%s", SharedConstants.REBROWSE_STAGING_DOMAIN),
          "Internal S2S User",
          UserRole.ADMIN,
          "internal-s2s",
          OffsetDateTime.parse("2020-12-03T10:15:30+01:00", RebrowseApi.DATE_TIME_FORMATTER),
          OffsetDateTime.parse("2020-12-03T10:15:30+01:00", RebrowseApi.DATE_TIME_FORMATTER),
          null,
          false);

  private UserRegistry() {}
}
