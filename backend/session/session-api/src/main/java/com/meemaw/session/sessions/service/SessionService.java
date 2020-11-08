package com.meemaw.session.sessions.service;

import lombok.extern.slf4j.Slf4j;

import com.meemaw.session.model.SessionDTO;
import com.meemaw.session.sessions.datasource.SessionDatasource;
import com.meemaw.shared.rest.query.SearchDTO;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
@Slf4j
public class SessionService {

  @Inject SessionDatasource sessionDatasource;

  public CompletionStage<Collection<SessionDTO>> getSessions(
      String organizationId, SearchDTO searchDTO) {
    return sessionDatasource.getSessions(organizationId, searchDTO);
  }

  public CompletionStage<Optional<SessionDTO>> getSession(UUID id, String organizationId) {
    return sessionDatasource.getSession(id, organizationId);
  }
}
