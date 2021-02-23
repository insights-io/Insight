package com.rebrowse.test.rest.data;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

public final class EventTestData {

  private static final String INCOMING = "/events/incoming";
  private static final String OUTGOING = "/events/outgoing/dto";

  private EventTestData() {}

  public static String readIncomingEvent(String fileName) throws IOException, URISyntaxException {
    return Files.readString(Path.of(resolveURI(String.join("/", INCOMING, fileName))));
  }

  public static String readOutgoingEvent(String fileName) throws IOException, URISyntaxException {
    return Files.readString(Path.of(resolveURI(String.join("/", OUTGOING, fileName))));
  }

  public static Collection<String> readIncomingEvents() throws URISyntaxException, IOException {
    return readDirectory(INCOMING);
  }

  public static Collection<String> readOutgoingEvents() throws URISyntaxException, IOException {
    return readDirectory(OUTGOING);
  }

  private static Collection<String> readDirectory(String resource)
      throws IOException, URISyntaxException {
    return Files.walk(Path.of(resolveURI(resource)))
        .filter(path -> !Files.isDirectory(path))
        .map(
            path -> {
              try {
                return Files.readString(path);
              } catch (IOException ex) {
                throw new RuntimeException(ex);
              }
            })
        .collect(Collectors.toList());
  }

  private static URI resolveURI(String resource) throws IOException, URISyntaxException {
    URI uri = EventTestData.class.getResource(resource).toURI();
    if ("jar".equals(uri.getScheme())) {
      for (FileSystemProvider provider : FileSystemProvider.installedProviders()) {
        if (provider.getScheme().equalsIgnoreCase("jar")) {
          try {
            provider.getFileSystem(uri);
          } catch (FileSystemNotFoundException e) {
            // in this case we need to initialize it first:
            provider.newFileSystem(uri, Collections.emptyMap());
          }
        }
      }
    }
    return uri;
  }
}
