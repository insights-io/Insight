package com.meemaw.session.events.service;

import com.meemaw.events.model.outgoing.dto.AbstractBrowserEventDTO;
import com.meemaw.shared.rest.query.SearchDTO;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

public interface EventsSearchService {

  CompletionStage<List<AbstractBrowserEventDTO>> search(
      UUID sessionId, String organizationId, SearchDTO searchDTO);
}
