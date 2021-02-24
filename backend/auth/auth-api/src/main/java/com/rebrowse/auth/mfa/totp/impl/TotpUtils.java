package com.rebrowse.auth.mfa.totp.impl;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.j256.twofactorauth.TimeBasedOneTimePasswordUtil;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Base64;
import javax.imageio.ImageIO;
import net.glxn.qrgen.javase.QRCode;

public final class TotpUtils {

  private TotpUtils() {}

  public static String generateSecret() {
    return TimeBasedOneTimePasswordUtil.generateBase32Secret();
  }

  public static int generateCurrentNumber(String secret) throws GeneralSecurityException {
    return TimeBasedOneTimePasswordUtil.generateCurrentNumber(secret);
  }

  public static boolean validate(String secret, int code) throws GeneralSecurityException {
    return TimeBasedOneTimePasswordUtil.validateCurrentNumber(secret, code, 0);
  }

  public static ByteArrayOutputStream generateQrImage(String keyId, String secret, String issuer) {
    String text =
        new StringBuffer()
            .append("otpauth://totp/")
            .append(keyId)
            .append("?secret=")
            .append(secret)
            .toString();

    return QRCode.from(text).withSize(200, 200).stream();
  }

  public static Result readBarcode(String base64) throws IOException, NotFoundException {
    return readBarcode(Base64.getDecoder().decode(base64));
  }

  public static Result readBarcode(byte[] buffer) throws IOException, NotFoundException {
    return new MultiFormatReader()
        .decode(
            new BinaryBitmap(
                new HybridBinarizer(
                    new BufferedImageLuminanceSource(
                        ImageIO.read(new ByteArrayInputStream(buffer))))));
  }
}
