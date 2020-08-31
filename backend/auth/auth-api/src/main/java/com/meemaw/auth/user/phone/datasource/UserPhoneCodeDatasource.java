package com.meemaw.auth.user.phone.datasource;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

public interface UserPhoneCodeDatasource {

  CompletionStage<Integer> setCode(UUID userId, int code);

  CompletionStage<Optional<Integer>> getCode(UUID userId);

  void deleteCode(UUID userId);
}
