package com.rebrowse.session.pages.datasource;

import com.fasterxml.jackson.databind.JsonNode;
import com.rebrowse.shared.rest.query.SearchDTO;
import com.rebrowse.shared.sql.client.SqlTransaction;
import com.rebrowse.location.model.Located;
import com.rebrowse.session.model.PageVisitCreateParams;
import com.rebrowse.session.model.PageVisitDTO;
import com.rebrowse.session.model.PageVisitSessionLink;
import com.rebrowse.useragent.model.HasUserAgent;
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
