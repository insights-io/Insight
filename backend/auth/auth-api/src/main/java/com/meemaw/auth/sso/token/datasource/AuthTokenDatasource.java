package com.meemaw.auth.sso.token.datasource;

import com.meemaw.auth.sso.token.model.CreateAuthTokenParams;
import com.meemaw.auth.sso.token.model.dto.AuthTokenDTO;
import com.meemaw.auth.user.model.AuthUser;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

public interface AuthTokenDatasource {

  CompletionStage<Optional<AuthUser>> getUser(String token);

  CompletionStage<AuthTokenDTO> createToken(CreateAuthTokenParams params);

  CompletionStage<Boolean> deleteToken(String token, UUID userId);
}
