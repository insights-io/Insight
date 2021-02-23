package com.rebrowse.auth.sso.session.datasource.hazelcast;

import com.hazelcast.map.EntryProcessor;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class CreateSessionEntryProcessor implements EntryProcessor<UUID, Set<String>, Void> {

  private final String sessionId;

  public CreateSessionEntryProcessor(String sessionId) {
    this.sessionId = sessionId;
  }

  @Override
  public Void process(Entry<UUID, Set<String>> entry) {
    Set<String> sessionIds = Optional.ofNullable(entry.getValue()).orElseGet(HashSet::new);
    sessionIds.add(sessionId);
    entry.setValue(sessionIds);
    return null;
  }
}
