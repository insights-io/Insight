package com.rebrowse.billing.webhook.service;

import java.util.concurrent.CompletionStage;

public interface WebhookProcessor<E> {

  CompletionStage<Void> process(String payload, String signature);

  CompletionStage<Void> process(E event);
}
