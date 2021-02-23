package com.rebrowse.auth.password.service;

import com.rebrowse.shared.SharedConstants;
import com.rebrowse.shared.context.RequestUtils;
import com.rebrowse.shared.rest.response.Boom;
import com.rebrowse.shared.sql.client.SqlPool;
import com.rebrowse.auth.accounts.model.request.AuthorizationRequest;
import com.rebrowse.auth.accounts.model.response.AuthorizationResponse;
import com.rebrowse.auth.core.MailingConstants;
import com.rebrowse.auth.mfa.MfaMethod;
import com.rebrowse.auth.organization.datasource.OrganizationPasswordPolicyDatasource;
import com.rebrowse.auth.password.datasource.PasswordDatasource;
import com.rebrowse.auth.password.datasource.PasswordResetDatasource;
import com.rebrowse.auth.password.model.PasswordPolicyValidator;
import com.rebrowse.auth.password.model.PasswordResetRequest;
import com.rebrowse.auth.password.model.PasswordValidationException;
import com.rebrowse.auth.sso.session.service.SsoService;
import com.rebrowse.auth.user.datasource.UserDatasource;
import com.rebrowse.auth.user.model.AuthUser;
import com.rebrowse.auth.user.model.UserWithLoginInformation;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.reactive.ReactiveMailer;
import io.quarkus.qute.Template;
import io.quarkus.qute.api.ResourcePath;
import io.smallrye.mutiny.tuples.Tuple3;
import java.net.URI;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.mindrot.jbcrypt.BCrypt;

@ApplicationScoped
public class PasswordService {

  private static final String PASSWORD_RESET_EMAIL_SENT_SUBJECT =
      String.format("Reset your %s password", SharedConstants.REBROWSE_ORGANIZATION_NAME);

  @Inject PasswordDatasource passwordDatasource;
  @Inject PasswordResetDatasource passwordResetDatasource;
  @Inject SsoService ssoService;
  @Inject SqlPool sqlPool;
  @Inject ReactiveMailer mailer;
  @Inject UserDatasource userDatasource;
  @Inject OrganizationPasswordPolicyDatasource organizationPasswordPolicyDatasource;

  @ResourcePath("password/password_reset")
  Template passwordResetTemplate;

  @ConfigProperty(name = "bcrypt.log_rounds")
  Integer logRounds;

  public CompletionStage<AuthorizationResponse> reset(
      UUID token, String password, URI serverBaseUri) {
    return passwordResetDatasource
        .retrieveWithLoginInformation(token)
        .thenCompose(
            maybeData -> {
              Tuple3<PasswordResetRequest, AuthUser, List<MfaMethod>> data =
                  maybeData.orElseThrow(
                      () ->
                          Boom.notFound().message("Password reset request not found").exception());

              PasswordResetRequest request = data.getItem1();
              if (request.hasExpired()) {
                throw Boom.badRequest().message("Password reset request expired").exception();
              }

              AuthUser user = data.getItem2();
              List<MfaMethod> methods = data.getItem3();

              return sqlPool
                  .beginTransaction()
                  .thenCompose(
                      transaction ->
                          passwordResetDatasource
                              .delete(request.getToken(), transaction)
                              .thenCompose(
                                  i1 ->
                                      passwordDatasource.storePassword(
                                          request.getUserId(), hashPassword(password), transaction))
                              .thenCompose(i1 -> transaction.commit())
                              .thenCompose(
                                  i1 -> {
                                    AuthorizationRequest authorizationRequest =
                                        new AuthorizationRequest(
                                            user.getEmail(),
                                            RequestUtils.sneakyUri(request.getRedirect()),
                                            serverBaseUri);

                                    return ssoService.authorize(
                                        user, methods, authorizationRequest);
                                  }));
            });
  }

  public CompletionStage<Optional<AuthUser>> forgotPassword(
      String email, URL redirect, URI resetLocation) {
    return userDatasource
        .retrieve(email)
        .thenCompose(
            maybeUser -> {
              // Don't leak that email is already in use
              if (maybeUser.isEmpty()) {
                return CompletableFuture.completedStage(maybeUser);
              }
              AuthUser user = maybeUser.get();
              return sqlPool
                  .beginTransaction()
                  .thenCompose(
                      transaction ->
                          passwordResetDatasource
                              .create(email, redirect, user.getId(), transaction)
                              .thenCompose(
                                  request ->
                                      sendPasswordResetEmail(request, resetLocation)
                                          .exceptionally(
                                              throwable -> {
                                                transaction.rollback();
                                                throw Boom.serverError()
                                                    .message("Failed to send password reset email")
                                                    .exception();
                                              }))
                              .thenCompose(nothing -> transaction.commit())
                              .thenApply(nothing -> Optional.of(user)));
            });
  }

  private CompletionStage<Void> sendPasswordResetEmail(
      PasswordResetRequest passwordResetRequest, URI resetLocation) {
    String passwordResetLocation =
        UriBuilder.fromUri(resetLocation).path("password-reset").build().toString();

    String email = passwordResetRequest.getEmail();
    UUID token = passwordResetRequest.getToken();

    return passwordResetTemplate
        .data("email", email)
        .data("token", token)
        .data("passwordResetURL", passwordResetLocation)
        .renderAsync()
        .thenCompose(
            html ->
                mailer
                    .send(
                        Mail.withHtml(email, PASSWORD_RESET_EMAIL_SENT_SUBJECT, html)
                            .setFrom(MailingConstants.FROM_SUPPORT))
                    .subscribeAsCompletionStage());
  }

  public CompletionStage<OffsetDateTime> changePassword(
      UUID userId,
      String email,
      String organizationId,
      String currentPassword,
      String newPassword) {
    return userDatasource
        .retrieveUserWithLoginInformation(email)
        .thenCompose(
            maybeUserWithPasswordHash -> {
              UserWithLoginInformation withLoginInformation =
                  maybeUserWithPasswordHash.orElseThrow(() -> Boom.notFound().exception());

              /*
               * If user signed up with social login he does not have a password yet.
               * we could just omit the check and store the new password here, but for semantics we
               * have it. User without a password should go through a password reset flow to get
               * one.
               */
              String hashedPassword = withLoginInformation.getPassword();
              if (hashedPassword == null || !BCrypt.checkpw(currentPassword, hashedPassword)) {
                throw Boom.badRequest().message("Current password miss match").exception();
              }

              return organizationPasswordPolicyDatasource
                  .retrieve(organizationId)
                  .thenCompose(
                      maybePolicy -> {
                        try {
                          PasswordPolicyValidator.validate(
                              maybePolicy.orElse(null), hashedPassword, newPassword);
                        } catch (PasswordValidationException ex) {
                          throw Boom.badRequest()
                              .errors(Map.of("newPassword", ex.getMessage()))
                              .exception();
                        }
                        return passwordDatasource.storePassword(userId, hashPassword(newPassword));
                      });
            });
  }

  public CompletionStage<UserWithLoginInformation> verifyPassword(String email, String password) {
    return userDatasource
        .retrieveUserWithLoginInformation(email)
        .thenApply(
            maybeUserWithPasswordHash -> {
              UserWithLoginInformation withLoginInformation =
                  maybeUserWithPasswordHash.orElseThrow(
                      () -> Boom.badRequest().message("Invalid email or password").exception());

              String hashedPassword = withLoginInformation.getPassword();
              if (hashedPassword == null) {
                throw Boom.badRequest().message("Invalid email or password").exception();
              }
              if (!BCrypt.checkpw(password, hashedPassword)) {
                throw Boom.badRequest().message("Invalid email or password").exception();
              }
              return withLoginInformation;
            });
  }

  public CompletionStage<Boolean> passwordResetRequestExists(UUID token) {
    return passwordResetDatasource.exists(token);
  }

  public String hashPassword(String password) {
    return BCrypt.hashpw(password, BCrypt.gensalt(logRounds));
  }
}
