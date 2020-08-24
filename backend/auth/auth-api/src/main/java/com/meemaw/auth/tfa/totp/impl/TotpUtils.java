package com.meemaw.auth.tfa.totp.impl;

import com.j256.twofactorauth.TimeBasedOneTimePasswordUtil;
import java.security.GeneralSecurityException;

public final class TotpUtils {

  private TotpUtils() {}

  public static String generateSecret() {
    return TimeBasedOneTimePasswordUtil.generateBase32Secret();
  }

  public static int generateCurrentNumber(String secret) throws GeneralSecurityException {
    return (int) TimeBasedOneTimePasswordUtil.generateCurrentNumber(secret);
  }

  public static boolean validate(String secret, int code) throws GeneralSecurityException {
    return TimeBasedOneTimePasswordUtil.validateCurrentNumber(secret, code, 0);
  }

  public static String generateQrImageURL(String keyId, String secret, String issuer) {
    return new StringBuilder(117 + keyId.length() + secret.length() + issuer.length())
        .append("https://chart.googleapis.com/chart")
        .append("?chs=200x200&cht=qr&chl=200x200&chld=M|0&cht=qr&chl=")
        .append("otpauth://totp/")
        .append(keyId)
        .append("?secret=")
        .append(secret)
        .append("&issuer=")
        .append(issuer)
        .toString();
  }
}
