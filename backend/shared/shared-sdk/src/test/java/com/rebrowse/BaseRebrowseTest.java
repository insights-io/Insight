package com.rebrowse;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class BaseRebrowseTest {

  protected String readFixture(String path) throws URISyntaxException, IOException {
    return Files.readString(Path.of(getClass().getResource(path).toURI()));
  }
}
