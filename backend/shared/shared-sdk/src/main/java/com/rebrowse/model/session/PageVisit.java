package com.rebrowse.model.session;

import com.rebrowse.net.ApiResource;
import com.rebrowse.net.RequestMethod;
import com.rebrowse.net.RequestOptions;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class PageVisit {

  UUID id;
  UUID sessionId;
  String organizationId;
  String doctype;
  URL origin;
  String path;
  String referrer;
  int height;
  int width;
  int screenHeight;
  int screenWidth;
  long compiledTimestamp;
  OffsetDateTime pageStart;

  public static CompletionStage<PageVisit> retrieve(UUID id, String organizationId) {
    return retrieve(id, organizationId, null);
  }

  public static CompletionStage<PageVisit> retrieve(
      UUID id, String organizationId, RequestOptions requestOptions) {
    return ApiResource.request(
        RequestMethod.GET,
        String.format("/v1/pages/%s?organizationId=%s", id, organizationId),
        PageVisit.class,
        requestOptions);
  }
}
