package com.rebrowse.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class BaseRebrowseTest {

  protected final ObjectMapper objectMapper = JacksonUtils.createObjectMapper();

  protected String readFixture(String path) throws URISyntaxException, IOException {
    return Files.readString(Path.of(getClass().getResource(path).toURI()));
  }
}
