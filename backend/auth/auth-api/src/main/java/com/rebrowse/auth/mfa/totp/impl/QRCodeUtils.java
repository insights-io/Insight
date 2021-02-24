package com.rebrowse.auth.mfa.totp.impl;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.rebrowse.shared.io.IoUtils;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import javax.imageio.ImageIO;

public final class QRCodeUtils {

  private static final Map<DecodeHintType, ?> QR_CODE_HINTS =
      Map.of(DecodeHintType.TRY_HARDER, Boolean.TRUE, DecodeHintType.PURE_BARCODE, Boolean.TRUE);

  private QRCodeUtils() {}

  public static String readQrImage(String base64) throws IOException, NotFoundException {
    InputStream is = new ByteArrayInputStream(IoUtils.base64decodeImage(base64));
    BufferedImage bufferedImage = ImageIO.read(is);
    LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
    BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
    Result result = new MultiFormatReader().decode(bitmap, QR_CODE_HINTS);
    return result.getText();
  }
}
