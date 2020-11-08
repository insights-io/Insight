package com.meemaw.test.setup;

import io.quarkus.mailer.Mail;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    tokenMatcher.matches();
    return tokenMatcher.group(1);
  }
}
