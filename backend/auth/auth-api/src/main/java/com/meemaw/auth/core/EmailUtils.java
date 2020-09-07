package com.meemaw.auth.core;

public final class EmailUtils {

  private EmailUtils() {}

  public static String domainFromEmail(String email) {
    return email.split("@")[1];
  }
}
