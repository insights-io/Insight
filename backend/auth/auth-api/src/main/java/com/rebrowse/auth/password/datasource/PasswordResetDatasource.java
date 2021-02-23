package com.rebrowse.auth.password.datasource;

import com.rebrowse.auth.mfa.MfaMethod;
import com.rebrowse.auth.password.model.PasswordResetRequest;
import com.rebrowse.auth.user.model.AuthUser;
import com.rebrowse.shared.sql.client.SqlTransaction;
import io.smallrye.mutiny.tuples.Tuple3;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

public interface PasswordResetDatasource {

  CompletionStage<Boolean> delete(UUID token, SqlTransaction transaction);

  CompletionStage<Boolean> exists(UUID token);

  CompletionStage<Optional<PasswordResetRequest>> retrieve(UUID token);

  CompletionStage<Optional<Tuple3<PasswordResetRequest, AuthUser, List<MfaMethod>>>>
      retrieveWithLoginInformation(UUID token);

  CompletionStage<PasswordResetRequest> create(
      String email, URL redirect, UUID userId, SqlTransaction transaction);
}
