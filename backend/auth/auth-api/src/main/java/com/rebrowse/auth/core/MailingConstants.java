package com.rebrowse.auth.core;

import com.rebrowse.shared.SharedConstants;

public final class MailingConstants {

  public static final String FROM_SUPPORT =
      String.format(
          "%s Support <support@%s>",
          SharedConstants.REBROWSE_ORGANIZATION_NAME, SharedConstants.REBROWSE_STAGING_DOMAIN);

  private MailingConstants() {}
}
