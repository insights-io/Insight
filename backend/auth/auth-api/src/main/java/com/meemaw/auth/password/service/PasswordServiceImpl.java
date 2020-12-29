package com.meemaw.auth.password.service;

import com.meemaw.auth.core.MailingConstants;
import com.meemaw.auth.organization.datasource.OrganizationPasswordPolicyDatasource;
import com.meemaw.auth.password.datasource.PasswordDatasource;
import com.meemaw.auth.password.datasource.PasswordResetDatasource;
import com.meemaw.auth.password.model.PasswordPolicyValidator;
import com.meemaw.auth.password.model.PasswordResetRequest;
import com.meemaw.auth.password.model.PasswordValidationException;
import com.meemaw.auth.user.datasource.UserDatasource;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.auth.user.model.UserWithLoginInformation;
import com.meemaw.shared.SharedConstants;
import com.meemaw.shared.rest.response.Boom;
import com.meemaw.shared.sql.client.SqlPool;
import com.meemaw.shared.sql.client.SqlTransaction;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.reactive.ReactiveMailer;
import io.quarkus.qute.Template;
import io.quarkus.qute.api.ResourcePath;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.opentracing.Traced;
import org.mindrot.jbcrypt.BCrypt;

@ApplicationScoped
@Slf4j
public class PasswordServiceImpl implements PasswordService {

  private static final String PASSWORD_RESET_EMAIL_SENT_SUBJECT =
      String.format("Reset your %s password", SharedConstants.ORGANIZATION_NAME);

  @Inject PasswordDatasource passwordDatasource;
  @Inject PasswordResetDatasource passwordResetDatasource;
  @Inject UserDatasource userDatasource;
  @Inject ReactiveMailer mailer;
  @Inject SqlPool sqlPool;
  @Inject OrganizationPasswordPolicyDatasource organizationPasswordPolicyDatasource;

  @ResourcePath("password/password_reset")
  Template passwordResetTemplate;

  @ConfigProperty(name = "bcrypt.log_rounds")
  Integer logRounds;

  @Override
  @Traced
  @Timed(
      name = "verifyPassword",
      description = "A measure of how long it takes to verify a password")
  public CompletionStage<UserWithLoginInformation> verifyPassword(String email, String password) {
    log.info("[AUTH]: verify password request for user: {}", email);
    return userDatasource
        .retrieveUserWithLoginInformation(email)
        .thenApply(
            maybeUserWithPasswordHash -> {
              UserWithLoginInformation withLoginInformation =
                  maybeUserWithPasswordHash.orElseThrow(
                      () -> Boom.badRequest().message("Invalid email or password").exception());

              String hashedPassword = withLoginInformation.getPassword();
              if (hashedPassword == null) {
                log.info("[AUTH]: Could not associate password with user={}", email);
                throw Boom.badRequest().message("Invalid email or password").exception();
              }

              if (!BCrypt.checkpw(password, hashedPassword)) {
                log.info("[AUTH]: Failed to verify password for user user={}", email);
                throw Boom.badRequest().message("Invalid email or password").exception();
              }

              return withLoginInformation;
            });
  }

  @Override
  @Traced
  @Timed(
      name = "forgotPassword",
      description = "A measure of how long it takes to forgot a password")
  public CompletionStage<Optional<AuthUser>> forgotPassword(
      String email, URL passwordResetBaseURL) {
    log.info("[AUTH]: Forgot password request for user: {}", email);
    String passwordResetLocation =
        UriBuilder.fromUri(passwordResetBaseURL.toString())
            .path("password-reset")
            .build()
            .toString();

    return userDatasource
        .retrieve(email)
        .thenCompose(
            maybeUser -> {
              // Don't leak that email is already in use
              if (maybeUser.isEmpty()) {
                log.info("[AUTH]: No user associated with email={}", email);
                return CompletableFuture.completedStage(maybeUser);
              }

              return this.forgotPassword(maybeUser.get(), passwordResetLocation)
                  .thenApply(ignores -> maybeUser);
            });
  }

  private CompletionStage<AuthUser> forgotPassword(AuthUser authUser, String passwordResetURL) {
    return sqlPool
        .beginTransaction()
        .thenCompose(transaction -> forgotPassword(authUser, passwordResetURL, transaction));
  }

  private CompletionStage<AuthUser> forgotPassword(
      AuthUser authUser, String passwordResetURL, SqlTransaction transaction) {
    String email = authUser.getEmail();
    UUID userId = authUser.getId();

    return passwordResetDatasource
        .create(email, userId, transaction)
        .thenApply(
            passwordResetRequest ->
                sendPasswordResetEmail(passwordResetRequest, passwordResetURL)
                    .exceptionally(
                        throwable -> {
                          transaction.rollback();
                          log.error(
                              "[AUTH] Failed to send password reset email: {} token: {}",
                              email,
                              passwordResetRequest.getToken(),
                              throwable);
                          throw Boom.serverError()
                              .message("Failed to send password reset email")
                              .exception();
                        }))
        .thenApply(nothing -> transaction.commit())
        .thenApply(nothing -> authUser);
  }

  private CompletionStage<Void> sendPasswordResetEmail(
      PasswordResetRequest passwordResetRequest, String passwordResetURL) {
    String email = passwordResetRequest.getEmail();
    UUID token = passwordResetRequest.getToken();
    log.info("[AUTH]: Sending password reset email to user={} token={}", email, token);

    return passwordResetTemplate
        .data("email", email)
        .data("token", token)
        .data("passwordResetURL", passwordResetURL)
        .renderAsync()
        .thenCompose(
            html ->
                mailer
                    .send(
                        Mail.withHtml(email, PASSWORD_RESET_EMAIL_SENT_SUBJECT, html)
                            .setFrom(MailingConstants.FROM_SUPPORT))
                    .subscribeAsCompletionStage());
  }

  @Override
  @Traced
  @Timed(name = "resetPassword", description = "A measure of how long it takes to reset a password")
  public CompletionStage<PasswordResetRequest> resetPassword(UUID token, String password) {
    log.info("[AUTH]: Reset password attempt token: {}", token);
    return passwordResetDatasource
        .retrieve(token)
        .thenApply(
            maybePasswordRequest ->
                maybePasswordRequest.orElseThrow(
                    () -> Boom.notFound().message("Password reset request not found").exception()))
        .thenCompose(passwordResetRequest -> reset(passwordResetRequest, password));
  }

  @Override
  @Traced
  @Timed(
      name = "changePassword",
      description = "A measure of how long it takes to change a password")
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

  private CompletionStage<PasswordResetRequest> reset(
      PasswordResetRequest request, String password) {
    if (request.hasExpired()) {
      log.info(
          "[AUTH]: Password reset request expired for user: {} token: {}",
          request.getEmail(),
          request.getToken());
      throw Boom.badRequest().message("Password reset request expired").exception();
    }

    return sqlPool
        .beginTransaction()
        .thenCompose(
            transaction ->
                passwordResetDatasource
                    .delete(request.getToken(), transaction)
                    .thenCompose(
                        deleted ->
                            createPassword(
                                request.getUserId(), request.getEmail(), password, transaction))
                    .thenCompose(created -> transaction.commit())
                    .thenApply(nothing -> request));
  }

  @Override
  @Traced
  @Timed(
      name = "createPassword",
      description = "A measure of how long it takes to create a password")
  public CompletionStage<Boolean> createPassword(
      UUID userId, String email, String password, SqlTransaction transaction) {
    log.info("[AUTH]: Create password request for user {}", email);
    return passwordDatasource
        .storePassword(userId, hashPassword(password), transaction)
        .thenApply(ignored -> true);
  }

  @Override
  @Traced
  @Timed(
      name = "passwordResetRequestExists",
      description = "A measure of how long it takes to check if password reset request exists")
  public CompletionStage<Boolean> passwordResetRequestExists(UUID token) {
    log.info("[AUTH]: Password reset request exists for token: {}", token);
    return passwordResetDatasource.retrieve(token).thenApply(Optional::isPresent);
  }

  @Override
  public String hashPassword(String password) {
    return BCrypt.hashpw(password, BCrypt.gensalt(logRounds));
  }
}
