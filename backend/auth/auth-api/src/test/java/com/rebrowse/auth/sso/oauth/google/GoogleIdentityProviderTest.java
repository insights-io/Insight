package com.rebrowse.auth.sso.oauth.google;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.rebrowse.auth.sso.AbstractIdentityProvider;
import io.quarkus.test.junit.QuarkusTest;
import javax.inject.Inject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

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
