package com.meemaw.auth.core;

import com.meemaw.shared.SharedConstants;

public final class MailingConstants {

  public static final String FROM_SUPPORT =
      String.format(
          "%s Support <support@%s>",
          SharedConstants.ORGANIZATION_NAME, SharedConstants.REBROWSE_STAGING_DOMAIN);

  private MailingConstants() {}
}
