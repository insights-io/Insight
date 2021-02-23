package com.rebrowse.session.sessions.datasource;

import com.fasterxml.jackson.databind.JsonNode;
import com.rebrowse.shared.rest.query.SearchDTO;
import com.rebrowse.shared.sql.client.SqlTransaction;
import com.rebrowse.location.model.Located;
import com.rebrowse.session.model.SessionDTO;
import com.rebrowse.useragent.model.HasUserAgent;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

public interface SessionDatasource {

  CompletionStage<Optional<UUID>> retrieveByDeviceId(String organizationId, UUID deviceId);

  CompletionStage<SessionDTO> create(
      UUID sessionId,
      UUID deviceId,
      String organizationId,
      Located location,
      HasUserAgent userAgent,
      SqlTransaction transaction);

  CompletionStage<SessionDTO> create(
      UUID sessionId,
      UUID deviceId,
      String organizationId,
      Located location,
      HasUserAgent userAgent);

  CompletionStage<Optional<SessionDTO>> retrieve(UUID id, String organizationId);

  CompletionStage<Collection<SessionDTO>> list(String organizationId, SearchDTO searchDTO);

  CompletionStage<JsonNode> count(String organizationId, SearchDTO searchDTO);

  CompletionStage<Collection<String>> distinct(
      Collection<String> on, String organizationId, SearchDTO searchDTO);
}
