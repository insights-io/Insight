package com.meemaw.shared.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.meemaw.events.model.external.UserEvent;
import com.meemaw.events.model.internal.AbstractBrowserEvent;
import com.meemaw.test.rest.mappers.JacksonMapper;
import java.util.Collection;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class UserEventDeserializationTest {

  @Test
  public void browserEventDeserialization() throws JsonProcessingException {
    String payload =
        "{\"event\":{\"t\":62171,\"e\":3,\"a\":[\"http://localhost:3002/_next/static/webpack/e07dcef65f82a25c7ded.hot-update.json\",\"resource\",62417.66500007361,8.584999945014715]},\"organizationId\":\"org123\",\"pageId\":\"d274c40c-357b-4357-8359-f33aed7b86df\",\"sessionId\":\"02737f08-28ba-47c4-9c13-5a2b6a133a9a\",\"deviceId\":\"14671e07-9081-4009-8752-a4d896c620c8\"}";
    UserEvent<?> deserialized = JacksonMapper.get().readValue(payload, UserEvent.class);

    assertEquals("org123", deserialized.getOrganizationId());
    assertEquals(
        UUID.fromString("02737f08-28ba-47c4-9c13-5a2b6a133a9a"), deserialized.getSessionId());
    assertEquals(UUID.fromString("d274c40c-357b-4357-8359-f33aed7b86df"), deserialized.getPageId());
  }

  @Test
  public void browserEventCollectionDeserialization() throws JsonProcessingException {
    String payload =
        "[{\"event\":{\"t\":62171,\"e\":3,\"a\":[\"http://localhost:3002/_next/static/webpack/e07dcef65f82a25c7ded.hot-update.json\",\"resource\",62417.66500007361,8.584999945014715]},\"organizationId\":\"org123\",\"pageId\":\"d274c40c-357b-4357-8359-f33aed7b86df\",\"sessionId\":\"02737f08-28ba-47c4-9c13-5a2b6a133a9a\",\"deviceId\":\"14671e07-9081-4009-8752-a4d896c620c8\"}]";
    Collection<UserEvent<AbstractBrowserEvent>> batch =
        JacksonMapper.get().readValue(payload, new TypeReference<>() {});
    assertEquals(1, batch.size());
  }
}
