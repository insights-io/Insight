package com.rebrowse.model.session;

import com.rebrowse.net.ApiResource;
import com.rebrowse.net.RequestMethod;
import com.rebrowse.net.RequestOptions;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class SessionPage {

  UUID id;
  UUID sessionId;
  String organizationId;
  String doctype;
  String url;
  String referrer;
  int height;
  int width;
  int screenHeight;
  int screenWidth;
  long compiledTimestamp;
  OffsetDateTime pageStart;

  public static CompletionStage<SessionPage> retrieve(UUID id, String organizationId) {
    return retrieve(id, organizationId, null);
  }

  public static CompletionStage<SessionPage> retrieve(
      UUID id, String organizationId, RequestOptions requestOptions) {
    return ApiResource.request(
        RequestMethod.GET,
        String.format("/v1/pages/%s?organizationId=%s", id, organizationId),
        SessionPage.class,
        requestOptions);
  }
}
