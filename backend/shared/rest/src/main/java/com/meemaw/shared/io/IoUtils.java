package com.meemaw.shared.io;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Base64;

public final class IoUtils {

  private IoUtils() {}

  public static String base64encodeImage(String imageURL) throws IOException {
    try (InputStream is = new URL(imageURL).openStream()) {
      byte[] buffer = is.readAllBytes();
      return Base64.getEncoder().encodeToString(buffer);
    }
  }
}
