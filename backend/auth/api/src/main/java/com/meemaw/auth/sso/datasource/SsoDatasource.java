package com.meemaw.auth.sso.datasource;

import com.meemaw.auth.sso.model.SsoUser;
import com.meemaw.shared.auth.UserDTO;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public interface SsoDatasource {

  CompletionStage<String> createSession(UserDTO user);

  CompletionStage<Optional<SsoUser>> findSession(String sessionId);

  CompletionStage<Boolean> deleteSession(String sessionId);


}
