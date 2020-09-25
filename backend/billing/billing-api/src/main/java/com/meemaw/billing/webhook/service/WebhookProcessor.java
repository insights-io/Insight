package com.meemaw.billing.webhook.service;

import java.util.concurrent.CompletionStage;

public interface WebhookProcessor<E> {

  CompletionStage<Boolean> process(String payload, String signature);

  CompletionStage<Boolean> process(E event);
}
