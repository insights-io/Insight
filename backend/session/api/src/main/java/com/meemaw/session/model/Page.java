package com.meemaw.session.model;

import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

@Getter
public class Page {

  private final String organization;
  private final UUID uid;
  private final String doctype;
  private final String url;
  private final String referrer;
  private final int height;
  private final int width;
  private final int screenHeight;
  private final int screenWidth;
  private final long compiledTimestamp;

  private Page(PageDTO dto) {
    this.organization = dto.getOrgId();
    this.uid = dto.getUid();
    this.doctype = dto.getDoctype();
    this.url = dto.getUrl();
    this.referrer = dto.getReferrer();
    this.height = dto.getHeight();
    this.width = dto.getWidth();
    this.screenHeight = dto.getScreenHeight();
    this.screenWidth = dto.getScreenWidth();
    this.compiledTimestamp = dto.getCompiledTs();
  }

  public static Page from(PageDTO dto) {
    return new Page(Objects.requireNonNull(dto));
  }
}
