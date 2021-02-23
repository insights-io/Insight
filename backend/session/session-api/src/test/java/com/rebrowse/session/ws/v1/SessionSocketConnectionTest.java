package com.rebrowse.session.ws.v1;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.quarkus.test.junit.QuarkusTest;
import java.util.concurrent.TimeUnit;
import javax.websocket.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTest
@Tag("integration")
public class SessionSocketConnectionTest extends AbstractSessionSocketTest {

  @BeforeEach
  public void cleanup() {
    MESSAGES.clear();
  }

  @Test
  public void testConnection() throws Exception {
    try (Session session = connect()) {
      assertEquals(String.format("OPEN %s", session.getId()), MESSAGES.poll(10, TimeUnit.SECONDS));
      session.close();
      assertEquals(String.format("CLOSE %s", session.getId()), MESSAGES.poll(10, TimeUnit.SECONDS));
    }
  }
}
