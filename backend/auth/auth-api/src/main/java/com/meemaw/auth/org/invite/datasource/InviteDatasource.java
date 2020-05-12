package com.meemaw.auth.org.invite.datasource;

import com.meemaw.auth.org.invite.model.dto.InviteCreateIdentifiedDTO;
import com.meemaw.auth.org.invite.model.dto.InviteDTO;
import io.vertx.axle.sqlclient.Transaction;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

public interface InviteDatasource {

  CompletionStage<Optional<InviteDTO>> find(String email, String org, UUID token);

  CompletionStage<Optional<InviteDTO>> findTransactional(Transaction transaction, String email,
      String org,
      UUID token);

  CompletionStage<List<InviteDTO>> findAll(String org);

  CompletionStage<Boolean> delete(UUID token, String org);

  CompletionStage<Boolean> deleteAll(Transaction transaction, String email, String org);

  CompletionStage<InviteDTO> create(Transaction transaction, InviteCreateIdentifiedDTO teamInvite);

}
