package com.meemaw.auth.org.invite.service;

import com.meemaw.auth.org.invite.datasource.InviteDatasource;
import com.meemaw.auth.org.invite.model.CanInviteSend;
import com.meemaw.auth.org.invite.model.dto.InviteAcceptDTO;
import com.meemaw.auth.org.invite.model.dto.InviteCreateDTO;
import com.meemaw.auth.org.invite.model.dto.InviteCreateIdentifiedDTO;
import com.meemaw.auth.org.invite.model.dto.InviteDTO;
import com.meemaw.auth.org.invite.model.dto.InviteSendDTO;
import com.meemaw.auth.user.datasource.UserDatasource;
import com.meemaw.shared.auth.InsightPrincipal;
import com.meemaw.shared.auth.UserRole;
import com.meemaw.shared.rest.response.Boom;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.reactive.ReactiveMailer;
import io.quarkus.qute.Template;
import io.quarkus.qute.api.ResourcePath;
import io.vertx.axle.pgclient.PgPool;
import io.vertx.axle.sqlclient.Transaction;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response.Status;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class InviteServiceImpl implements InviteService {

  @ResourcePath("org/invite")
  Template inviteTemplate;

  @Inject
  ReactiveMailer mailer;

  @Inject
  PgPool pgPool;

  @Inject
  InviteDatasource inviteDatasource;

  @Inject
  UserDatasource userDatasource;

  private static final String FROM_SUPPORT = "Insight Support <support@insight.com>";

  @Override
  public CompletionStage<InviteDTO> create(InviteCreateDTO inviteCreate,
      InsightPrincipal principal) {

    InviteCreateIdentifiedDTO teamInvite = new InviteCreateIdentifiedDTO(inviteCreate.getEmail(),
        principal.getOrg(), inviteCreate.getRole(), principal.getUserId());

    return pgPool.begin().thenCompose(transaction -> createTransactional(transaction, teamInvite));
  }

  private CompletionStage<InviteDTO> createTransactional(Transaction transaction,
      InviteCreateIdentifiedDTO teamInviteCreate) {
    String email = teamInviteCreate.getEmail();
    String org = teamInviteCreate.getOrg();
    log.info("Inviting user {} to org {} with role {} by {}", email, org,
        teamInviteCreate.getRole(),
        teamInviteCreate.getCreator());

    return inviteDatasource.create(transaction, teamInviteCreate)
        .thenCompose(teamInvite -> sendInviteEmail(teamInvite.getToken(), teamInvite)
            .exceptionally(throwable -> {
              transaction.rollback();
              log.error("Failed to send invite user={} org={}", email, org, throwable);
              throw Boom.serverError().message("Failed to send invite email").exception();
            })
            .thenCompose(x -> transaction.commit())
            .thenApply(x -> {
              log.info("Invite sent to user={} org={} token={}", email, org, teamInvite.getToken());
              return teamInvite;
            }));
  }

  @Override
  public CompletionStage<Boolean> accept(InviteAcceptDTO inviteAccept) {
    return pgPool.begin()
        .thenCompose(transaction -> acceptTransactional(transaction, inviteAccept));
  }

  private CompletionStage<Boolean> acceptTransactional(Transaction transaction,
      InviteAcceptDTO inviteAccept) {
    String email = inviteAccept.getEmail();
    String org = inviteAccept.getOrg();
    UUID token = inviteAccept.getToken();

    log.info("Accepting invitation user {} to org {} via token {}", email, org, token);

    return inviteDatasource.findTransactional(transaction, email, org, token)
        .thenApply(maybeTeamInvite -> {
          InviteDTO teamInvite = maybeTeamInvite.orElseThrow(() -> {
            log.info("Invite does not exist user={} org={} token={}", email, org, token);
            throw Boom.badRequest().message("Team invite does not exist.").exception();
          });

          if (teamInvite.hasExpired()) {
            log.info("Team invite expired user={} org={} token={}", email, org, token);
            throw Boom.badRequest().message("Team invite expired").exception();
          }
          return teamInvite;
        })
        .thenCompose(teamInvite -> {
          UserRole role = teamInvite.getRole();
          return userDatasource.createUser(transaction, email, org, role);
        })
        .thenCompose(userId -> inviteDatasource.deleteAll(transaction, email, org))
        .thenCompose(deleted -> transaction.commit().thenApply(x -> {
          log.info("Invite accepted user={} org={} token={}", email, org, token);
          return deleted;
        }));
  }

  @Override
  public CompletionStage<Void> send(InviteSendDTO inviteSend, InsightPrincipal principal) {
    String org = principal.getOrg();
    String email = inviteSend.getEmail();
    UUID token = inviteSend.getToken();
    log.info("Sending invite to user {} org {} token {}", email, org, token);

    return inviteDatasource.find(email, org, token)
        .thenApply(maybeInvite -> maybeInvite.orElseThrow(() -> {
          log.error("Failed to find invite for user={} org={}", email, org);
          throw Boom.status(Status.NOT_FOUND).exception();
        }))
        .thenCompose(invite -> sendInviteEmail(token, invite));
  }

  @Override
  public CompletionStage<Boolean> delete(UUID token, InsightPrincipal principal) {
    String org = principal.getOrg();
    UUID userId = principal.getUserId();
    log.info("User {} org {} deleting invite {}", userId, org, token);

    return inviteDatasource.delete(token, org).thenApply(deleted -> {
      if (!deleted) {
        throw Boom.status(Status.NOT_FOUND).exception();
      }
      return true;
    });
  }

  @Override
  public CompletionStage<List<InviteDTO>> list(InsightPrincipal principal) {
    return inviteDatasource.findAll(principal.getOrg());
  }

  private CompletionStage<Void> sendInviteEmail(UUID token, CanInviteSend canInvite) {
    String email = canInvite.getEmail();
    String subject = "You've been invited to Insight";

    return inviteTemplate
        .data("creator", canInvite.getCreator())
        .data("email", email)
        .data("token", token)
        .data("role", canInvite.getRole())
        .data("orgId", canInvite.getOrg())
        .renderAsync()
        .thenCompose(
            html -> mailer.send(Mail.withHtml(email, subject, html).setFrom(FROM_SUPPORT))
                .subscribeAsCompletionStage());
  }
}
