package com.meemaw.session.pages.datasource;

import com.fasterxml.jackson.databind.JsonNode;
import com.meemaw.location.model.Located;
import com.meemaw.session.model.PageVisitCreateParams;
import com.meemaw.session.model.PageVisitDTO;
import com.meemaw.session.model.PageVisitSessionLink;
import com.meemaw.shared.rest.query.SearchDTO;
import com.meemaw.shared.sql.client.SqlTransaction;
import com.meemaw.useragent.model.HasUserAgent;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

public interface PageVisitDatasource {

  CompletionStage<JsonNode> count(String organizationId, SearchDTO searchDTO);

  CompletionStage<PageVisitSessionLink> create(
      UUID pageId, UUID sessionId, UUID deviceId, PageVisitCreateParams page);

  CompletionStage<PageVisitSessionLink> create(
      UUID id,
      UUID sessionId,
      UUID deviceId,
      PageVisitCreateParams page,
      SqlTransaction transaction);

  CompletionStage<Optional<PageVisitDTO>> retrieve(UUID id, String organizationId);

  CompletionStage<PageVisitSessionLink> createPageAndNewSession(
      UUID pageId,
      UUID sessionId,
      UUID deviceId,
      HasUserAgent userAgent,
      Located location,
      PageVisitCreateParams createParams);
}
