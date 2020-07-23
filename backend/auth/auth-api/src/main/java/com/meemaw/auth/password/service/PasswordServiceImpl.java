package com.meemaw.auth.password.service;

import com.meemaw.auth.core.MailingConstants;
import com.meemaw.auth.password.datasource.PasswordDatasource;
import com.meemaw.auth.password.datasource.PasswordResetDatasource;
import com.meemaw.auth.password.model.PasswordResetRequest;
import com.meemaw.auth.user.datasource.UserDatasource;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.auth.user.model.UserWithHashedPassword;
import com.meemaw.shared.rest.exception.BoomException;
import com.meemaw.shared.rest.exception.DatabaseException;
import com.meemaw.shared.rest.response.Boom;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.reactive.ReactiveMailer;
import io.quarkus.qute.Template;
import io.quarkus.qute.api.ResourcePath;
import io.vertx.axle.pgclient.PgPool;
import io.vertx.axle.sqlclient.Transaction;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.opentracing.Traced;
import org.mindrot.jbcrypt.BCrypt;

@ApplicationScoped
@Slf4j
public class PasswordServiceImpl implements PasswordService {

  @Inject PasswordDatasource passwordDatasource;
  @Inject PasswordResetDatasource passwordResetDatasource;
  @Inject UserDatasource userDatasource;
  @Inject ReactiveMailer mailer;
  @Inject PgPool pgPool;

  @ResourcePath("password/password_reset")
  Template passwordResetTemplate;

  @Override
  @Traced
  @Timed(
      name = "verifyPassword",
      description = "A measure of how long it takes to verify a password")
  public CompletionStage<AuthUser> verifyPassword(String email, String password) {
    log.info("[AUTH]: verify password request for user: {}", email);
    return passwordDatasource
        .findUserWithPassword(email)
        .thenApply(
            maybeUserWithPasswordHash -> {
              UserWithHashedPassword withPassword =
                  maybeUserWithPasswordHash.orElseThrow(
                      () -> {
                        throw Boom.badRequest().message("Invalid email or password").exception();
                      });

              String hashedPassword = withPassword.getPassword();
              if (hashedPassword == null) {
                log.info("[AUTH]: Could not associate password with user={}", email);
                throw Boom.badRequest().message("Invalid email or password").exception();
              }

              if (!BCrypt.checkpw(password, hashedPassword)) {
                log.info("[AUTH]: Failed to verify password for user user: {}", email);
                throw Boom.badRequest().message("Invalid email or password").exception();
              }

              return withPassword.user();
            })
        .exceptionally(
            throwable -> {
              Throwable cause = throwable.getCause();
              if (cause instanceof BoomException) {
                throw (BoomException) cause;
              }
              log.error(
                  "[AUTH]: Failed to retrieve user with password hash for user: {}",
                  email,
                  throwable);
              throw new DatabaseException(throwable);
            });
  }

  @Override
  @Traced
  @Timed(
      name = "forgotPassword",
      description = "A measure of how long it takes to forgot a password")
  public CompletionStage<Optional<AuthUser>> forgotPassword(
      String email, String passwordResetBaseURL) {
    log.info("[AUTH]: Forgot password request for user: {}", email);
    String passwordResetURL = String.join("/", passwordResetBaseURL, "password-reset");

    return userDatasource
        .findUser(email)
        .thenCompose(
            maybeUser -> {
              // Don't leak that email is in use
              if (maybeUser.isEmpty()) {
                log.info("[AUTH]: No user associated with email: {}", email);
                return CompletableFuture.completedStage(maybeUser);
              }

              return this.forgotPassword(maybeUser.get(), passwordResetURL)
                  .thenApply(ignores -> maybeUser);
            });
  }

  private CompletionStage<AuthUser> forgotPassword(AuthUser authUser, String passwordResetURL) {
    return pgPool
        .begin()
        .thenCompose(transaction -> forgotPassword(authUser, passwordResetURL, transaction));
  }

  private CompletionStage<AuthUser> forgotPassword(
      AuthUser authUser, String passwordResetURL, Transaction transaction) {
    String email = authUser.getEmail();
    UUID userId = authUser.getId();

    return passwordResetDatasource
        .createPasswordResetRequest(email, userId, transaction)
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
    log.info("[AUTH]: Sending password reset email to user: {} token: {}", email, token);
    String subject = "Reset your Insight password";

    return passwordResetTemplate
        .data("email", email)
        .data("token", token)
        .data("passwordResetURL", passwordResetURL)
        .renderAsync()
        .thenCompose(
            html ->
                mailer
                    .send(
                        Mail.withHtml(email, subject, html).setFrom(MailingConstants.FROM_SUPPORT))
                    .subscribeAsCompletionStage());
  }

  @Override
  @Traced
  @Timed(name = "resetPassword", description = "A measure of how long it takes to reset a password")
  public CompletionStage<PasswordResetRequest> resetPassword(UUID token, String password) {
    log.info("[AUTH]: Reset password attempt token: {}", token);
    return passwordResetDatasource
        .findPasswordResetRequest(token)
        .thenApply(
            maybePasswordRequest ->
                maybePasswordRequest.orElseThrow(
                    () -> {
                      throw new BoomException(
                          Boom.notFound().message("Password reset request not found"));
                    }))
        .thenCompose(passwordResetRequest -> reset(passwordResetRequest, password));
  }

  @Override
  @Traced
  @Timed(
      name = "changePassword",
      description = "A measure of how long it takes to change a password")
  public CompletionStage<Boolean> changePassword(
      UUID userId,
      String email,
      String currentPassword,
      String newPassword,
      String confirmNewPassword) {
    if (currentPassword.equals(newPassword)) {
      throw Boom.badRequest()
          .message("New password cannot be the same as the previous one!")
          .exception();
    }

    if (!confirmNewPassword.equals(newPassword)) {
      throw Boom.badRequest().message("Passwords must match!").exception();
    }

    return passwordDatasource
        .findUserWithPassword(email)
        .thenCompose(
            maybeUserWithPasswordHash -> {
              UserWithHashedPassword userWithHashedPassword =
                  maybeUserWithPasswordHash.orElseThrow(
                      () -> {
                        throw Boom.notFound().exception();
                      });

              /*
               * If user signed up with social login he does not have a password yet.
               * we could just omit the check and store the new password here, but for semantics we
               * have it. User without a password should go through a password reset flow to get
               * one.
               */
              String hashedPassword = userWithHashedPassword.getPassword();
              if (hashedPassword == null || !BCrypt.checkpw(currentPassword, hashedPassword)) {
                throw Boom.badRequest().message("Current password miss match").exception();
              }

              return passwordDatasource.storePassword(userId, hashPassword(newPassword));
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

    return pgPool
        .begin()
        .thenCompose(
            transaction ->
                passwordResetDatasource
                    .deletePasswordResetRequest(request.getToken(), transaction)
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
      UUID userId, String email, String password, Transaction transaction) {
    log.info("[AUTH]: Create password request for user {}", email);
    return passwordDatasource
        .storePassword(userId, hashPassword(password), transaction)
        .thenApply(x -> true);
  }

  @Override
  @Traced
  @Timed(
      name = "passwordResetRequestExists",
      description = "A measure of how long it takes to check if password reset request exists")
  public CompletionStage<Boolean> passwordResetRequestExists(UUID token) {
    log.info("[AUTH]: Password reset request exists for token: {}", token);
    return passwordResetDatasource.findPasswordResetRequest(token).thenApply(Optional::isPresent);
  }
}
