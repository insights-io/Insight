package com.rebrowse.auth.user.phone.datasource;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

public interface UserPhoneCodeDatasource {

  CompletionStage<Integer> setCode(String key, int code);

  CompletionStage<Optional<Integer>> getCode(String key);

  void deleteCode(String key);
}
