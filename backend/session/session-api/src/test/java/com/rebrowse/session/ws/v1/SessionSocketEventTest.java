package com.rebrowse.session.ws.v1;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rebrowse.events.model.incoming.AbstractBrowserEvent;
import com.rebrowse.events.stream.EventsStream;
import com.rebrowse.test.testconainers.kafka.KafkaTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.websocket.DeploymentException;
import javax.websocket.Session;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTest
@Tag("integration")
@QuarkusTestResource(KafkaTestResource.class)
public class SessionSocketEventTest extends AbstractSessionSocketTest {

  @Inject
  @Channel(EventsStream.UNLOAD)
  Emitter<AbstractBrowserEvent> emitter;

  @Inject ObjectMapper objectMapper;

  @Test
  public void unloadEventMessageReception()
      throws IOException, DeploymentException, InterruptedException {
    try (Session session = connect()) {
      assertEquals(String.format("OPEN %s", session.getId()), MESSAGES.poll(10, TimeUnit.SECONDS));

      // unload event
      String unloadEventPayload = "{\"t\": 1234, \"e\": \"1\", \"a\": [\"http://localhost:8080\"]}";
      AbstractBrowserEvent unloadEvent =
          objectMapper.readValue(unloadEventPayload, AbstractBrowserEvent.class);
      emitter.send(unloadEvent);
      assertEquals("PAGE END", MESSAGES.poll(10, TimeUnit.SECONDS));
    }
  }
}
