package com.meemaw.search.events.resource;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class EventsResourceImpl implements EventsResource {

  @Override
  public CompletionStage<String> search() {
    return CompletableFuture.completedStage("TODO");
  }
}
