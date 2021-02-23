package com.rebrowse.auth.core;

import com.rebrowse.shared.rest.response.Boom;
import com.rebrowse.api.RebrowseApi;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class EmailUtils {

  private static final String FREE_EMAIL_PROVIDERS_FILE_NAME = "free-email-providers.txt";
  private static Set<String> freeEmailProviders;

  private EmailUtils() {}

  public static String getDomain(String email) {
    return email.split("@")[1];
  }

  public static boolean isBusinessDomain(String domain) {
    return !getFreeEmailProviders().contains(domain);
  }

  private static Set<String> getFreeEmailProviders() {
    if (freeEmailProviders == null) {
      log.info("[AUTH] Loading free email providers from={}", FREE_EMAIL_PROVIDERS_FILE_NAME);

      try (BufferedReader reader =
          new BufferedReader(
              new InputStreamReader(
                  Thread.currentThread()
                      .getContextClassLoader()
                      .getResourceAsStream(FREE_EMAIL_PROVIDERS_FILE_NAME),
                  RebrowseApi.CHARSET))) {
        Set<String> providers = new HashSet<>();
        while (reader.ready()) {
          providers.add(reader.readLine().trim());
        }
        freeEmailProviders = providers;
      } catch (IOException ex) {
        log.error(
            "[AUTH] Failed to load free email providers from={}",
            FREE_EMAIL_PROVIDERS_FILE_NAME,
            ex);
        throw Boom.serverError().exception(ex);
      }
    }

    return freeEmailProviders;
  }
}
