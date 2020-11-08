package com.meemaw.session.useragent.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import com.meemaw.useragent.model.UserAgentDTO;

import javax.inject.Inject;

@QuarkusTest
public class UserAgentServiceTest {

  @Inject UserAgentService userAgentService;

  @Test
  public void test_user_agent_service__desktop() {
    assertEquals(
        new UserAgentDTO("Desktop", "Mac OS X", "Chrome"),
        userAgentService.parse(
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/84.0.4147.89 Safari/537.36"));
  }

  @Test
  public void test_user_agent_service__mobile() {
    assertEquals(
        new UserAgentDTO("Phone", "Android", "Chrome"),
        userAgentService.parse(
            "Mozilla/5.0 (Linux; Android 10; VOG-L29) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/84.0.4147.89 Mobile Safari/537.36"));
  }
}
