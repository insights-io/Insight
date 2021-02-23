package com.rebrowse.auth.sso.token.datasource;

import com.rebrowse.auth.sso.token.model.CreateAuthTokenParams;
import com.rebrowse.auth.sso.token.model.dto.AuthTokenDTO;
import com.rebrowse.auth.user.model.AuthUser;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

public interface AuthTokenDatasource {

  CompletionStage<List<AuthTokenDTO>> list(UUID userId);

  CompletionStage<AuthTokenDTO> create(CreateAuthTokenParams params);

  CompletionStage<Boolean> delete(String token, UUID userId);

  CompletionStage<Optional<AuthUser>> getTokenUser(String token);
}
