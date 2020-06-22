package com.meemaw.session.service;

import com.meemaw.session.datasource.SessionDatasource;
import com.meemaw.session.model.SessionDTO;
import com.meemaw.shared.rest.query.SearchDTO;
import io.smallrye.mutiny.Uni;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class SessionService {

  @Inject SessionDatasource sessionDatasource;

  public Uni<Collection<SessionDTO>> getSessions(String organizationId, SearchDTO searchDTO) {
    return sessionDatasource.getSessions(organizationId, searchDTO);
  }

  public Uni<Optional<SessionDTO>> getSession(UUID id, String organizationId) {
    return sessionDatasource.getSession(id, organizationId);
  }
}
