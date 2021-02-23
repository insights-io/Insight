package com.rebrowse.auth.user.phone.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.quarkus.test.junit.QuarkusTest;
import javax.inject.Inject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTest
@Tag("integration")
public class UserPhoneCodeServiceTest {

  @Inject UserPhoneCodeService userPhoneCodeService;

  @Test
  public void user_phone_code_service__should_always_generate_code_of_same_length() {
    for (int i = 0; i < 100; i++) {
      int code = userPhoneCodeService.newCode();
      assertEquals(UserPhoneCodeService.CODE_LENGTH, String.valueOf(code).length());
    }
  }
}
