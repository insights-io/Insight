package com.meemaw.auth.sso.oauth.google;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.meemaw.auth.sso.AbstractIdentityProvider;

import javax.inject.Inject;

@QuarkusTest
@Tag("integration")
public class GoogleIdentityProviderTest {

  @Inject GoogleIdentityProvider googleService;

  @Test
  public void google_service_secure_state_should_be_of_fixed_length() {
    String data = "data";
    for (int i = 0; i < 100; i++) {
      String secureData = googleService.secureState(data);
      assertEquals(
          AbstractIdentityProvider.SECURE_STATE_PREFIX_LENGTH + data.length(), secureData.length());
    }
  }
}
