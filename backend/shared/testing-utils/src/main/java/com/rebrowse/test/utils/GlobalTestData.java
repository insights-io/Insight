package com.rebrowse.test.utils;

import com.rebrowse.shared.SharedConstants;
import com.rebrowse.shared.context.RequestUtils;
import java.net.URI;
import java.net.URL;
import java.util.UUID;

public final class GlobalTestData {

  public static final String LOCALHOST_REDIRECT = "http://localhost:3000/test";
  public static final URL LOCALHOST_REDIRECT_URL = RequestUtils.sneakyUrl(LOCALHOST_REDIRECT);
  public static final URI LOCALHOST_REDIRECT_URI = RequestUtils.sneakyUri(LOCALHOST_REDIRECT_URL);

  public static final String REBROWSE_ADMIN_EMAIL =
      String.format("admin@%s", SharedConstants.REBROWSE_STAGING_DOMAIN);
  public static final String REBROWSE_ADMIN_FULL_NAME = "Admin Admin";
  public static final String REBROWSE_ADMIN_PASSWORD = "superDuperPassword123";
  public static final UUID REBROWSE_ADMIN_ID =
      UUID.fromString("7c071176-d186-40ac-aaf8-ac9779ab047b");

  private GlobalTestData() {}
}
