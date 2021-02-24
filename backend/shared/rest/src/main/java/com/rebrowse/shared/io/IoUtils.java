package com.rebrowse.shared.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Base64;

public final class IoUtils {

  private IoUtils() {}

  public static String base64encodeImage(String imageUrl) throws IOException {
    try (InputStream is = new URL(imageUrl).openStream()) {
      return base64encodeImage(is.readAllBytes());
    }
  }

  public static String base64encodeImage(ByteArrayOutputStream image) {
    return base64encodeImage(image.toByteArray());
  }

  public static String base64encodeImage(byte[] buffer) {
    return Base64.getEncoder().encodeToString(buffer);
  }

  public static byte[] base64decodeImage(String base64) {
    return Base64.getDecoder().decode(base64);
  }
}
