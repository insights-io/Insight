package com.rebrowse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rebrowse.net.ApiResource;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class BaseRebrowseTest {

  protected final ObjectMapper objectMapper = ApiResource.OBJECT_MAPPER;

  protected String readFixture(String path) throws URISyntaxException, IOException {
    return Files.readString(Path.of(getClass().getResource(path).toURI()));
  }
}
