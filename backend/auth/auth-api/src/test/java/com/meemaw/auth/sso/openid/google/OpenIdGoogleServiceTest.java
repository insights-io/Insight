package com.meemaw.auth.sso.openid.google;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.meemaw.auth.sso.openid.shared.AbstractOpenIdService;
import io.quarkus.test.junit.QuarkusTest;
import javax.inject.Inject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTest
@Tag("integration")
public class OpenIdGoogleServiceTest {

  @Inject OpenIdGoogleService googleService;

  @Test
  public void google_service_secure_state_should_be_of_fixed_length() {
    String data = "data";
    for (int i = 0; i < 100; i++) {
      String secureData = googleService.secureState(data);
      assertEquals(
          AbstractOpenIdService.SECURE_STATE_PREFIX_LENGTH + data.length(), secureData.length());
    }
  }
}
