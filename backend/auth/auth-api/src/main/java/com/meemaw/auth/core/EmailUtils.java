package com.meemaw.auth.core;

import lombok.extern.slf4j.Slf4j;

import com.meemaw.shared.rest.response.Boom;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

@Slf4j
public final class EmailUtils {

  private static Set<String> freeEmailProviders;

  private EmailUtils() {}

  public static String domainFromEmail(String email) {
    return email.split("@")[1];
  }

  public static boolean isBusinessDomain(String domain) {
    return !getFreeEmailProviders().contains(domain);
  }

  private static Set<String> getFreeEmailProviders() {
    if (freeEmailProviders == null) {
      String resource = "free-email-providers.txt";
      log.info("[AUTH] Loading free email providers from={}", resource);
      try (BufferedReader reader =
          new BufferedReader(
              new InputStreamReader(
                  Thread.currentThread().getContextClassLoader().getResourceAsStream(resource),
                  StandardCharsets.UTF_8))) {
        Set<String> providers = new HashSet<>();
        while (reader.ready()) {
          providers.add(reader.readLine().trim());
        }
        freeEmailProviders = providers;
      } catch (IOException ex) {
        log.error("[AUTH] Failed to load free email providers from={}", resource, ex);
        throw Boom.serverError().exception(ex);
      }
    }

    return freeEmailProviders;
  }
}
