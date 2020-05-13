package com.meemaw.session.model;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class Page {

  UUID id;
  UUID sessionID;
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
}
