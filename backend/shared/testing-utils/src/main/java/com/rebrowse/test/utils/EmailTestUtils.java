package com.rebrowse.test.utils;

import io.quarkus.mailer.Mail;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public final class EmailTestUtils {

  private EmailTestUtils() {}

  public static String parseLink(Mail mail) {
    Document htmlDocument = Jsoup.parse(mail.getHtml());
    Elements link = htmlDocument.select("a");
    return link.attr("href");
  }

  public static String parseConfirmationToken(Mail mail) {
    return parseConfirmationToken(parseLink(mail));
  }

  public static String parseConfirmationToken(String link) {
    Matcher tokenMatcher = Pattern.compile("^.*token=(.*)$").matcher(link);
    if (!tokenMatcher.matches()) {
      throw new RuntimeException();
    }
    return tokenMatcher.group(1);
  }

  public static String parseTokenFromSignUpCompleteLink(String link) {
    Matcher matcher = Pattern.compile("^.*/v1/signup/(.*)/complete$").matcher(link);
    if (!matcher.matches()) {
      throw new RuntimeException();
    }
    return matcher.group(1);
  }

  public static String randomBusinessEmail() {
    return String.format("%s@%s.com", UUID.randomUUID(), UUID.randomUUID());
  }
}
