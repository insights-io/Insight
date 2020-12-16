package com.meemaw.session.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.meemaw.test.rest.mappers.JacksonMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class PageVisitSessionLinkSerializationTest {

  @Test
  public void pageIdentitySerializationTest() throws JsonProcessingException {
    String payload =
        "{\"deviceId\":\"9babcb5a-2249-4f50-93ee-8634118e684e\",\"sessionId\":\"a895a4f4-aaab-43f1-b617-b7f050c9e054\",\"pageVisitId\":\"842d1623-d6a6-4d49-a16b-98df700467f9\"}";

    PageVisitSessionLink deserialized =
        JacksonMapper.get().readValue(payload, PageVisitSessionLink.class);

    assertEquals(PageVisitSessionLink.class, deserialized.getClass());

    assertEquals(
        UUID.fromString("9babcb5a-2249-4f50-93ee-8634118e684e"), deserialized.getDeviceId());
  }
}
