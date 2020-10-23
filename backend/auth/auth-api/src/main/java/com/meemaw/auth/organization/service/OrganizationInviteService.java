package com.meemaw.auth.organization.service;

import com.meemaw.auth.core.MailingConstants;
import com.meemaw.auth.organization.datasource.OrganizationDatasource;
import com.meemaw.auth.organization.datasource.OrganizationPasswordPolicyDatasource;
import com.meemaw.auth.organization.datasource.OrganizationTeamInviteDatasource;
import com.meemaw.auth.organization.model.Organization;
import com.meemaw.auth.organization.model.TeamInviteTemplateData;
import com.meemaw.auth.organization.model.dto.TeamInviteAcceptDTO;
import com.meemaw.auth.organization.model.dto.TeamInviteCreateDTO;
import com.meemaw.auth.organization.model.dto.TeamInviteDTO;
import com.meemaw.auth.password.datasource.PasswordDatasource;
import com.meemaw.auth.password.model.PasswordPolicyValidator;
import com.meemaw.auth.password.model.PasswordValidationException;
import com.meemaw.auth.password.service.PasswordService;
import com.meemaw.auth.sso.session.model.InsightPrincipal;
import com.meemaw.auth.user.datasource.UserDatasource;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.shared.logging.LoggingConstants;
import com.meemaw.shared.rest.exception.BoomException;
import com.meemaw.shared.rest.response.Boom;
import com.meemaw.shared.sql.client.SqlPool;
import com.meemaw.shared.sql.client.SqlTransaction;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.reactive.ReactiveMailer;
import io.quarkus.qute.Template;
import io.quarkus.qute.api.ResourcePath;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response.Status;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.microprofile.opentracing.Traced;
import org.slf4j.MDC;

@ApplicationScoped
@Slf4j
public class OrganizationInviteService {

  @Inject ReactiveMailer mailer;
  @Inject SqlPool sqlPool;
  @Inject UserDatasource userDatasource;
  @Inject OrganizationDatasource organizationDatasource;
  @Inject OrganizationTeamInviteDatasource teamInviteDatasource;
  @Inject OrganizationPasswordPolicyDatasource organizationPasswordPolicyDatasource;
  @Inject PasswordDatasource passwordDatasource;
  @Inject PasswordService passwordService;

  @ResourcePath("organization/team_invite")
  Template teamInviteTemplate;

  @Traced
  public CompletionStage<TeamInviteDTO> createTeamInvite(
      TeamInviteCreateDTO invite, AuthUser creator, String acceptLink) {
    return sqlPool
        .beginTransaction()
        .thenCompose(transaction -> createTeamInvite(invite, creator, acceptLink, transaction));
  }

  private CompletionStage<TeamInviteDTO> createTeamInvite(
      TeamInviteCreateDTO invite, AuthUser creator, String acceptLink, SqlTransaction transaction) {
    String organizationId = creator.getOrganizationId();
    String invitedEmail = invite.getEmail();
    log.info(
        "[AUTH]: Creating team invite for user={} organizationId={}", invitedEmail, organizationId);

    return userDatasource
        .findUser(invitedEmail, transaction)
        .thenCompose(
            maybeUser -> {
              // If user is not in organization we should not leak that it is already registered
              if (maybeUser.map(AuthUser::getOrganizationId).orElse("").equals(organizationId)) {
                throw teamInviteCreateUserExistsException(invitedEmail, organizationId);
              }

              return organizationDatasource
                  .findOrganization(organizationId, transaction)
                  .thenCompose(
                      maybeOrganization -> {
                        Organization organization =
                            maybeOrganization.orElseThrow(() -> Boom.notFound().exception());

                        TeamInviteTemplateData teamInviteTemplateData =
                            new TeamInviteTemplateData(
                                invitedEmail,
                                invite.getRole(),
                                creator.getFullName(),
                                organization.getName());

                        return teamInviteDatasource
                            .create(
                                organizationId,
                                creator.getId(),
                                teamInviteTemplateData,
                                transaction)
                            .thenCompose(
                                teamInvite ->
                                    sendInviteEmail(
                                            teamInvite.getToken(),
                                            teamInviteTemplateData,
                                            acceptLink)
                                        .thenApply(ignored -> teamInvite))
                            .thenCompose(
                                teamInvite ->
                                    transaction
                                        .commit()
                                        .thenApply(
                                            ignored -> {
                                              log.info(
                                                  "[AUTH]: User={} accepted team invite organizationId={}",
                                                  teamInvite.getEmail(),
                                                  teamInvite.getOrganizationId());
                                              return teamInvite;
                                            }))
                            .exceptionally(
                                throwable -> {
                                  log.error(
                                      "[AUTH]: Failed to accept team invite={} creator={}",
                                      invite,
                                      creator,
                                      throwable);
                                  transaction.rollback();
                                  throw (CompletionException) throwable;
                                });
                      });
            });
  }

  private BoomException teamInviteCreateUserExistsException(String email, String organizationId) {
    log.info(
        "[AUTH]: User with provided email={} is already in your organization={}",
        email,
        organizationId);

    throw Boom.badRequest()
        .errors(Map.of("email", "User with provided email is already in your organization"))
        .exception();
  }

  @Traced
  public CompletionStage<AuthUser> acceptTeamInvite(UUID token, TeamInviteAcceptDTO inviteAccept) {
    return sqlPool
        .beginTransaction()
        .thenCompose(transaction -> acceptTeamInvite(token, inviteAccept, transaction));
  }

  private CompletionStage<AuthUser> acceptTeamInvite(
      UUID token, TeamInviteAcceptDTO inviteAccept, SqlTransaction transaction) {
    String password = inviteAccept.getPassword();
    String fullName = inviteAccept.getFullName();

    return teamInviteDatasource
        .retrieve(token, transaction)
        .thenApply(
            maybeTeamInvite -> {
              TeamInviteDTO teamInvite =
                  maybeTeamInvite.orElseThrow(
                      () -> Boom.badRequest().message("Team invite does not exist.").exception());

              MDC.put(LoggingConstants.USER_EMAIL, teamInvite.getEmail());
              MDC.put(LoggingConstants.ORGANIZATION_ID, teamInvite.getOrganizationId());

              if (teamInvite.hasExpired()) {
                log.info(
                    "[AUTH]: Team invite has expired for user={} organizationId={}",
                    teamInvite.getEmail(),
                    teamInvite.getOrganizationId());
                throw Boom.badRequest().message("Team invite expired").exception();
              }

              log.info(
                  "[AUTH]: Accepting team invite attempt for user={} organizationId={}",
                  teamInvite.getEmail(),
                  teamInvite.getOrganizationId());

              return teamInvite;
            })
        .thenCompose(
            teamInvite ->
                organizationPasswordPolicyDatasource
                    .retrieve(teamInvite.getOrganizationId())
                    .thenAccept(
                        maybePolicy -> {
                          try {
                            PasswordPolicyValidator.validateFirstPassword(
                                maybePolicy.orElse(null), password);
                          } catch (PasswordValidationException ex) {
                            log.debug(
                                "[AUTH]: Failed to accept team invite due to password policy violation",
                                ex);

                            throw Boom.badRequest()
                                .errors(Map.of("password", ex.getMessage()))
                                .exception(ex);
                          }
                        })
                    .thenCompose(
                        ignored ->
                            userDatasource.createUser(
                                teamInvite.getEmail(),
                                fullName,
                                teamInvite.getOrganizationId(),
                                teamInvite.getRole(),
                                null,
                                transaction)))
        .thenCompose(
            user ->
                passwordDatasource
                    .storePassword(
                        user.getId(), passwordService.hashPassword(password), transaction)
                    .thenCompose(
                        ignored ->
                            teamInviteDatasource.delete(
                                user.getEmail(), user.getOrganizationId(), transaction))
                    .thenCompose(deleted -> transaction.commit().thenApply(ignored -> user))
                    .exceptionally(
                        throwable -> {
                          log.error(
                              "[AUTH]: Failed to accept team invite={} token={}",
                              inviteAccept,
                              token);
                          transaction.rollback();
                          throw (CompletionException) throwable;
                        })
                    .thenApply(ignored -> user));
  }

  @Traced
  public CompletionStage<TeamInviteDTO> sendTeamInvite(
      UUID token, InsightPrincipal principal, String acceptInviteURL) {
    String creatorFullName = principal.user().getFullName();

    return teamInviteDatasource
        .retrieveWithOrganization(token)
        .thenCompose(
            maybeTeamInviteAndOrganization -> {
              Pair<TeamInviteDTO, Organization> teamInviteOrganization =
                  maybeTeamInviteAndOrganization.orElseThrow(() -> Boom.notFound().exception());
              Organization organization = teamInviteOrganization.getRight();
              TeamInviteDTO teamInvite = teamInviteOrganization.getLeft();

              return sendInviteEmail(
                      token,
                      new TeamInviteTemplateData(
                          teamInvite.getEmail(),
                          teamInvite.getRole(),
                          creatorFullName,
                          organization.getName()),
                      acceptInviteURL)
                  .thenApply(ignored -> teamInvite);
            });
  }

  @Traced
  public CompletionStage<Boolean> deleteTeamInvite(UUID token, InsightPrincipal principal) {
    AuthUser user = principal.user();
    log.debug("[AUTH]: Delete team invite attempt for token={} user={}", token, user.getId());
    return teamInviteDatasource
        .delete(token)
        .thenApply(
            deleted -> {
              if (!deleted) {
                throw Boom.status(Status.NOT_FOUND).exception();
              }
              log.info("[AUTH]: Team invite deleted token={} principal={}", token, user.getId());
              return true;
            });
  }

  @Traced
  public CompletionStage<List<TeamInviteDTO>> listTeamInvites(InsightPrincipal principal) {
    AuthUser user = principal.user();
    log.debug("[AUTH]: List team invites for organizationId={}", user.getOrganizationId());
    return teamInviteDatasource.list(user.getOrganizationId());
  }

  private CompletionStage<Void> sendInviteEmail(
      UUID token, TeamInviteTemplateData templateData, String acceptLink) {
    String subject = "You've been invited to Insight";
    log.info(
        "[AUTH]: Sending team invite email to user={} token={} acceptLink={}",
        templateData.getRecipientEmail(),
        token,
        acceptLink);

    return teamInviteTemplate
        .data("creator_full_name", templateData.getCreatorFullName())
        .data("token", token)
        .data("role", templateData.getRecipientRole())
        .data("company", templateData.getOrganizationName())
        .data("acceptInviteURL", acceptLink)
        .renderAsync()
        .thenCompose(
            html ->
                mailer
                    .send(
                        Mail.withHtml(templateData.getRecipientEmail(), subject, html)
                            .setFrom(MailingConstants.FROM_SUPPORT))
                    .subscribeAsCompletionStage());
  }
}
