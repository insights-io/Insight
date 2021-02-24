package com.rebrowse.auth.sso.session.datasource.hazelcast;

import com.hazelcast.map.EntryProcessor;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

public class DeleteSessionEntryProcessor implements EntryProcessor<UUID, Set<String>, Void> {

  private final String sessionId;

  public DeleteSessionEntryProcessor(String sessionId) {
    this.sessionId = sessionId;
  }

  @Override
  public Void process(Entry<UUID, Set<String>> entry) {
    Set<String> sessions = entry.getValue();
    sessions.remove(sessionId);
    if (sessions.isEmpty()) {
      entry.setValue(null);
    } else {
      entry.setValue(sessions);
    }
    return null;
  }
}
