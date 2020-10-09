package com.rebrowse.model.organization;

import com.rebrowse.Rebrowse;
import com.rebrowse.net.ApiResource;
import com.rebrowse.net.RequestOptions;
import java.time.OffsetDateTime;
import java.util.concurrent.CompletionStage;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class Organization {

  String id;
  String name;
  OffsetDateTime createdAt;
  OffsetDateTime updatedAt;

  public static CompletionStage<Organization> retrieve(RequestOptions requestOptions) {
    String url = String.format("%s%s", Rebrowse.apiBase(), "/v1/organization");
    return ApiResource.get(url, Organization.class, requestOptions);
  }

  public static CompletionStage<Organization> retrieve(String id, RequestOptions requestOptions) {
    String url = String.format("%s%s%s", Rebrowse.apiBase(), "/v1/organization/", id);
    return ApiResource.get(url, Organization.class, requestOptions);
  }
}
