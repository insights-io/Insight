package com.meemaw.test.setup;

import static io.restassured.config.RedirectConfig.redirectConfig;
import static io.restassured.config.RestAssuredConfig.newConfig;

import io.restassured.config.RestAssuredConfig;

public final class RestAssuredUtils {

  private RestAssuredUtils() {}

  public static RestAssuredConfig dontFollowRedirects() {
    return newConfig().redirect(redirectConfig().followRedirects(false));
  }
}
