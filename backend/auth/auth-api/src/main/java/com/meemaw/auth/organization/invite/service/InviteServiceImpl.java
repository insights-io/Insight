package com.meemaw.auth.organization.invite.service;

import com.meemaw.auth.core.MailingConstants;
import com.meemaw.auth.organization.datasource.OrganizationDatasource;
import com.meemaw.auth.organization.invite.datasource.InviteDatasource;
import com.meemaw.auth.organization.invite.model.TeamInvite;
import com.meemaw.auth.organization.invite.model.TeamInviteTemplateData;
import com.meemaw.auth.organization.invite.model.dto.InviteAcceptDTO;
import com.meemaw.auth.organization.invite.model.dto.InviteCreateDTO;
import com.meemaw.auth.organization.model.Organization;
import com.meemaw.auth.sso.model.InsightPrincipal;
import com.meemaw.auth.user.datasource.UserDatasource;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.auth.user.model.UserRole;
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
import org.apache.commons.lang3.tuple.Pair;

@ApplicationScoped
@Slf4j
public class InviteServiceImpl implements InviteService {

  @Inject ReactiveMailer mailer;
  @Inject PgPool pgPool;
  @Inject InviteDatasource inviteDatasource;
  @Inject UserDatasource userDatasource;
  @Inject OrganizationDatasource organizationDatasource;

  @ResourcePath("organization/team_invite")
  Template teamInviteTemplate;

  @Override
  public CompletionStage<TeamInvite> createTeamInvite(
      InviteCreateDTO invite, InsightPrincipal principal, String acceptInviteURL) {
    AuthUser authUser = principal.user();
    return pgPool
        .begin()
        .thenCompose(
            transaction ->
                organizationDatasource
                    .findOrganization(authUser.getOrg(), transaction)
                    .thenCompose(
                        maybeOrganization -> {
                          Organization organization =
                              maybeOrganization.orElseThrow(() -> Boom.notFound().exception());

                          TeamInviteTemplateData teamInviteTemplateData =
                              new TeamInviteTemplateData(
                                  invite.getEmail(),
                                  invite.getRole(),
                                  authUser.getFullName(),
                                  organization.getName());
                          return createTeamInvite(
                              authUser.getOrg(),
                              authUser.getId(),
                              teamInviteTemplateData,
                              acceptInviteURL,
                              transaction);
                        }));
  }

  private CompletionStage<TeamInvite> createTeamInvite(
      String organizationId,
      UUID creatorId,
      TeamInviteTemplateData teamInviteTemplateData,
      String acceptInviteURL,
      Transaction transaction) {
    return inviteDatasource
        .createTeamInvite(organizationId, creatorId, teamInviteTemplateData, transaction)
        .thenCompose(
            teamInvite ->
                sendInviteEmail(teamInvite.getToken(), teamInviteTemplateData, acceptInviteURL)
                    .exceptionally(
                        throwable -> {
                          transaction.rollback();
                          throw Boom.serverError()
                              .message("Failed to send invite email")
                              .exception();
                        })
                    .thenCompose(x -> transaction.commit())
                    .thenApply(x -> teamInvite));
  }

  @Override
  public CompletionStage<Boolean> acceptTeamInvite(UUID token, InviteAcceptDTO inviteAccept) {
    return pgPool
        .begin()
        .thenCompose(transaction -> acceptTeamInvite(token, inviteAccept, transaction));
  }

  private CompletionStage<Boolean> acceptTeamInvite(
      UUID token, InviteAcceptDTO inviteAccept, Transaction transaction) {

    return inviteDatasource
        .findTeamInvite(token, transaction)
        .thenApply(
            maybeTeamInvite -> {
              TeamInvite teamInvite =
                  maybeTeamInvite.orElseThrow(
                      () -> Boom.badRequest().message("Team invite does not exist.").exception());

              if (teamInvite.hasExpired()) {
                throw Boom.badRequest().message("Team invite expired").exception();
              }
              return teamInvite;
            })
        .thenCompose(
            teamInvite -> {
              UserRole role = teamInvite.getRole();
              return userDatasource.createUser(
                  teamInvite.getEmail(),
                  inviteAccept.getFullName(),
                  teamInvite.getOrganizationId(),
                  role,
                  transaction);
            })
        .thenCompose(
            user -> inviteDatasource.deleteTeamInvites(user.getEmail(), user.getOrg(), transaction))
        .thenCompose(deleted -> transaction.commit().thenApply(x -> deleted));
  }

  @Override
  public CompletionStage<Void> sendTeamInvite(
      UUID token, InsightPrincipal principal, String acceptInviteURL) {
    String creatorFullName = principal.user().getFullName();

    return inviteDatasource
        .findTeamInviteWithOrganization(token)
        .thenCompose(
            maybeTeamInviteAndOrganization -> {
              Pair<TeamInvite, Organization> teamInviteOrganization =
                  maybeTeamInviteAndOrganization.orElseThrow(() -> Boom.notFound().exception());
              Organization organization = teamInviteOrganization.getRight();
              TeamInvite teamInvite = teamInviteOrganization.getLeft();

              return sendInviteEmail(
                  token,
                  new TeamInviteTemplateData(
                      teamInvite.getEmail(),
                      teamInvite.getRole(),
                      creatorFullName,
                      organization.getName()),
                  acceptInviteURL);
            });
  }

  @Override
  public CompletionStage<Boolean> deleteTeamInvite(UUID token, InsightPrincipal principal) {
    return inviteDatasource
        .deleteTeamInvite(token)
        .thenApply(
            deleted -> {
              if (!deleted) {
                throw Boom.status(Status.NOT_FOUND).exception();
              }
              return true;
            });
  }

  @Override
  public CompletionStage<List<TeamInvite>> listTeamInvites(InsightPrincipal principal) {
    return inviteDatasource.findTeamInvites(principal.user().getOrg());
  }

  private CompletionStage<Void> sendInviteEmail(
      UUID token, TeamInviteTemplateData teamInviteTemplateData, String acceptInviteURL) {
    String subject = "You've been invited to Insight";

    return teamInviteTemplate
        .data("creator_full_name", teamInviteTemplateData.getCreatorFullName())
        .data("token", token)
        .data("role", teamInviteTemplateData.getRecipientRole())
        .data("company", teamInviteTemplateData.getOrganizationName())
        .data("acceptInviteURL", acceptInviteURL)
        .renderAsync()
        .thenCompose(
            html ->
                mailer
                    .send(
                        Mail.withHtml(teamInviteTemplateData.getRecipientEmail(), subject, html)
                            .setFrom(MailingConstants.FROM_SUPPORT))
                    .subscribeAsCompletionStage());
  }
}
