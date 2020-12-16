package com.meemaw.session.sessions.datasource;

import com.fasterxml.jackson.databind.JsonNode;
import com.meemaw.location.model.Located;
import com.meemaw.session.model.SessionDTO;
import com.meemaw.shared.rest.query.SearchDTO;
import com.meemaw.shared.sql.client.SqlTransaction;
import com.meemaw.useragent.model.HasUserAgent;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

public interface SessionDatasource {

  CompletionStage<Optional<UUID>> findSessionDeviceLink(String organizationId, UUID deviceId);

  CompletionStage<SessionDTO> createSession(
      UUID sessionId,
      UUID deviceId,
      String organizationId,
      Located location,
      HasUserAgent userAgent,
      SqlTransaction transaction);

  CompletionStage<SessionDTO> createSession(
      UUID sessionId,
      UUID deviceId,
      String organizationId,
      Located location,
      HasUserAgent userAgent);

  CompletionStage<Optional<SessionDTO>> getSession(UUID id, String organizationId);

  CompletionStage<Collection<SessionDTO>> getSessions(String organizationId, SearchDTO searchDTO);

  CompletionStage<JsonNode> count(String organizationId, SearchDTO searchDTO);

  CompletionStage<Collection<String>> distinct(
      Collection<String> on, String organizationId, SearchDTO searchDTO);
}
