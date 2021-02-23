package com.rebrowse.auth.utils;

import com.google.zxing.NotFoundException;
import com.rebrowse.api.RebrowseApi;
import com.rebrowse.auth.mfa.totp.impl.QRCodeUtils;
import com.rebrowse.auth.user.model.dto.PhoneNumberDTO;
import com.rebrowse.shared.SharedConstants;
import com.rebrowse.shared.sms.MockSmsbox;
import com.rebrowse.shared.sms.SmsMessage;
import com.rebrowse.test.utils.GlobalTestData;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AuthApiTestUtils {

  private AuthApiTestUtils() {}

  public static int getLastSmsMessageVerificationCode(
      MockSmsbox mockSmsbox, PhoneNumberDTO sentTo) {
    return getLastSmsMessageVerificationCode(mockSmsbox, sentTo.getNumber());
  }

  public static int getLastSmsMessageVerificationCode(MockSmsbox mockSmsbox, String sentTo) {
    List<SmsMessage> messages = mockSmsbox.getMessagesSentTo(sentTo);
    SmsMessage message = messages.get(messages.size() - 1);
    Pattern pattern =
        Pattern.compile(
            String.format(
                "^.*\\[%s\\] Verification code: (.*).*$",
                SharedConstants.REBROWSE_ORGANIZATION_NAME));

    Matcher matcher = pattern.matcher(message.getBody());
    if (!matcher.matches()) {
      throw new RuntimeException();
    }

    return Integer.parseInt(matcher.group(1));
  }

  public static String getSecretFromQrCode(String issuer, String base64image, String email)
      throws IOException, NotFoundException {
    String pattern = "^otpauth:\\/\\/totp\\/" + issuer + ":" + email + "\\?secret=(.*)$";
    Pattern totpQrCodeSecretPattern = Pattern.compile(pattern);
    String text = QRCodeUtils.readQrImage(base64image);
    Matcher matcher = totpQrCodeSecretPattern.matcher(text);
    if (!matcher.matches()) {
      throw new RuntimeException(
          "Failed to extract secret from text=" + text + " pattern=" + pattern);
    }
    return matcher.group(1);
  }

  public static String microsoftOAuthAuthorizePattern(String clientId, URI callback, String email) {
    return "^"
        + "https:\\/\\/login\\.microsoftonline\\.com\\/common\\/oauth2\\/v2\\.0\\/authorize"
        + "\\?client_id="
        + clientId
        + "&redirect_uri="
        + URLEncoder.encode(callback.toString(), RebrowseApi.CHARSET)
        + "&response_type=code"
        + "&scope=openid\\+email\\+profile"
        + "&response_mode=query"
        + "&login_hint="
        + URLEncoder.encode(email, RebrowseApi.CHARSET)
        + "&state=(.*)"
        + URLEncoder.encode(GlobalTestData.LOCALHOST_REDIRECT, RebrowseApi.CHARSET)
        + "$";
  }

  public static String googleOAuthAuthorizePattern(String clientId, URI callback, String email) {
    return "^"
        + "https:\\/\\/accounts\\.google\\.com\\/o\\/oauth2\\/auth"
        + "\\?client_id="
        + clientId
        + "&redirect_uri="
        + URLEncoder.encode(callback.toString(), RebrowseApi.CHARSET)
        + "&response_type=code&scope=openid\\+email\\+profile"
        + "&login_hint="
        + URLEncoder.encode(email, RebrowseApi.CHARSET)
        + "&state=(.*)"
        + URLEncoder.encode(GlobalTestData.LOCALHOST_REDIRECT, RebrowseApi.CHARSET)
        + "$";
  }

  public static String githubOAuthAuthorizePattern(String clientId, URI callback, String email) {
    return "^"
        + "https:\\/\\/github\\.com\\/login\\/oauth\\/authorize"
        + "\\?client_id="
        + clientId
        + "&redirect_uri="
        + URLEncoder.encode(callback.toString(), RebrowseApi.CHARSET)
        + "&response_type=code"
        + "&scope=read%3Auser\\+user%3Aemail"
        + "&login="
        + URLEncoder.encode(email, RebrowseApi.CHARSET)
        + "&state=(.*)"
        + URLEncoder.encode(GlobalTestData.LOCALHOST_REDIRECT, RebrowseApi.CHARSET)
        + "$";
  }

  public static String randomBusinessEmail() {
    return String.format("%s@%s.com", UUID.randomUUID(), UUID.randomUUID());
  }
}
