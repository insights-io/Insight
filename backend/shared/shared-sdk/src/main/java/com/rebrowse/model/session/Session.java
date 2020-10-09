package com.rebrowse.model.session;

import com.rebrowse.Rebrowse;
import com.rebrowse.net.ApiResource;
import com.rebrowse.net.RequestOptions;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class Session {

  UUID id;
  UUID deviceId;
  String organizationId;
  Location location;
  UserAgent userAgent;
  OffsetDateTime createdAt;

  public static CompletionStage<Session> retrieve(UUID id, RequestOptions requestOptions) {
    String url = String.format("%s%s%s", Rebrowse.apiBase(), "/v1/sessions/", id);
    return ApiResource.get(url, Session.class, requestOptions);
  }
}
