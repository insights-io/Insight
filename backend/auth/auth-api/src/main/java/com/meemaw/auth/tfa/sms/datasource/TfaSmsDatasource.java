package com.meemaw.auth.tfa.sms.datasource;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

public interface TfaSmsDatasource {

  CompletionStage<Integer> setCode(UUID userId, int code);

  CompletionStage<Optional<Integer>> getCode(UUID userId);

  void deleteCode(UUID userId);
}
