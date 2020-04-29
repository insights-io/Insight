package com.meemaw.session.model;

import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Page {

  String orgID;
  UUID uid;
  String doctype;
  String url;
  String referrer;
  int height;
  int width;
  int screenHeight;
  int screenWidth;
  long compiledTimestamp;

  Page(PageDTO dto) {
    this(dto.getOrgId(),
        dto.getUid(),
        dto.getDoctype(),
        dto.getUrl(),
        dto.getReferrer(),
        dto.getHeight(),
        dto.getWidth(),
        dto.getScreenHeight(),
        dto.getScreenWidth(),
        dto.getCompiledTs());

  }

  public static Page from(PageDTO dto) {
    return new Page(Objects.requireNonNull(dto));
  }
}
