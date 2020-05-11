package com.meemaw.auth.password.service;

import com.meemaw.auth.password.datasource.PasswordDatasource;
import com.meemaw.auth.password.datasource.PasswordResetDatasource;
import com.meemaw.auth.password.model.PasswordResetRequest;
import com.meemaw.auth.password.model.dto.PasswordResetRequestDTO;
import com.meemaw.auth.signup.datasource.SignupDatasource;
import com.meemaw.auth.user.datasource.UserDatasource;
import com.meemaw.auth.user.model.UserWithHashedPasswordDTO;
import com.meemaw.shared.auth.UserDTO;
import com.meemaw.shared.rest.exception.BoomException;
import com.meemaw.shared.rest.exception.DatabaseException;
import com.meemaw.shared.rest.response.Boom;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.reactive.ReactiveMailer;
import io.quarkus.qute.Template;
import io.quarkus.qute.api.ResourcePath;
import io.vertx.axle.pgclient.PgPool;
import io.vertx.axle.sqlclient.Transaction;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;

@ApplicationScoped
@Slf4j
public class PasswordServiceImpl implements PasswordService {

  @Inject PasswordDatasource passwordDatasource;

  @Inject PasswordResetDatasource passwordResetDatasource;

  @Inject UserDatasource userDatasource;

  @Inject SignupDatasource signupDatasource;

  @ResourcePath("password/reset")
  Template passwordResetTemplate;

  @Inject ReactiveMailer mailer;

  @Inject PgPool pgPool;

  private static final String FROM_SUPPORT = "Insight Support <support@insight.com>";

  public CompletionStage<UserDTO> verifyPassword(String email, String password) {
    return passwordDatasource
        .findUserWithPassword(email)
        .thenApply(
            maybeUserWithPasswordHash -> {
              UserWithHashedPasswordDTO withPassword =
                  maybeUserWithPasswordHash.orElseThrow(
                      () -> {
                        log.info("User {} not found", email);
                        throw new BoomException(
                            Boom.badRequest().message("Invalid email or password"));
                      });

              String hashedPassword = withPassword.getPassword();
              if (hashedPassword == null) {
                log.info("Could not associate password with user={}", email);
                throw Boom.badRequest().message("Invalid email or password").exception();
              }

              if (!BCrypt.checkpw(password, hashedPassword)) {
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
              log.error("Failed to retrieve user with password hash", throwable);
              throw new DatabaseException(throwable);
            });
  }

  @Override
  public CompletionStage<Boolean> forgot(String email) {
    return userDatasource
        .findUser(email)
        .thenApply(
            maybeUser ->
                maybeUser.orElseThrow(
                    () -> {
                      log.info("User {} not found", email);
                      throw new BoomException(Boom.notFound().message("User not found"));
                    }))
        .thenCompose(this::forgot);
  }

  private CompletionStage<Boolean> forgot(UserDTO userDTO) {
    return pgPool.begin().thenCompose(transaction -> forgotTransactional(transaction, userDTO));
  }

  private CompletionStage<Boolean> forgotTransactional(Transaction transaction, UserDTO userDTO) {
    String email = userDTO.getEmail();
    String org = userDTO.getOrg();
    UUID userId = userDTO.getId();

    return passwordResetDatasource
        .create(transaction, email, userId, org)
        .thenApply(
            passwordResetRequest ->
                sendPasswordResetEmail(passwordResetRequest)
                    .exceptionally(
                        throwable -> {
                          transaction.rollback();
                          log.error(
                              "Failed to send password reset email={} org={}",
                              email,
                              org,
                              throwable);
                          throw Boom.serverError()
                              .message("Failed to send password reset email")
                              .exception();
                        }))
        .thenApply(nothing -> transaction.commit())
        .thenApply(nothing -> true);
  }

  private CompletionStage<Void> sendPasswordResetEmail(PasswordResetRequest passwordResetRequest) {
    String email = passwordResetRequest.getEmail();
    UUID token = passwordResetRequest.getToken();
    String org = passwordResetRequest.getOrg();
    String subject = "Reset your Insight password";

    return passwordResetTemplate
        .data("email", email)
        .data("orgId", org)
        .data("token", token)
        .renderAsync()
        .thenCompose(
            html ->
                mailer
                    .send(Mail.withHtml(email, subject, html).setFrom(FROM_SUPPORT))
                    .subscribeAsCompletionStage());
  }

  @Override
  public CompletionStage<Boolean> reset(PasswordResetRequestDTO passwordResetRequestDTO) {
    UUID token = passwordResetRequestDTO.getToken();
    String email = passwordResetRequestDTO.getEmail();
    String org = passwordResetRequestDTO.getOrg();
    String password = passwordResetRequestDTO.getPassword();
    return passwordResetDatasource
        .find(token, email, org)
        .thenApply(
            maybePasswordRequest ->
                maybePasswordRequest.orElseThrow(
                    () -> {
                      log.info("Password reset request not found email={}", email);
                      throw new BoomException(
                          Boom.notFound().message("Password reset request not found"));
                    }))
        .thenCompose(passwordResetRequest -> reset(passwordResetRequest, password));
  }

  private CompletionStage<Boolean> reset(
      PasswordResetRequest passwordResetRequest, String newPassword) {
    UUID token = passwordResetRequest.getToken();
    String email = passwordResetRequest.getEmail();
    String org = passwordResetRequest.getOrg();
    UUID userId = passwordResetRequest.getUserId();

    if (passwordResetRequest.hasExpired()) {
      log.info("Password reset request expired email={} org={} token={}", email, org, token);
      throw Boom.badRequest().message("Password reset request expired").exception();
    }

    return pgPool
        .begin()
        .thenCompose(
            transaction ->
                passwordResetDatasource
                    .delete(transaction, token, email, org)
                    .thenCompose(
                        deleted -> signupDatasource.delete(transaction, email, org, userId))
                    .thenCompose(deleted -> create(transaction, userId, email, org, newPassword))
                    .thenCompose(created -> transaction.commit())
                    .thenApply(nothing -> true));
  }

  public CompletionStage<Boolean> create(
      Transaction transaction, UUID userId, String email, String org, String password) {
    log.info("Storing password email={} userId={} org={}", email, userId, org);
    String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(13));

    return passwordDatasource
        .create(transaction, userId, hashedPassword)
        .thenApply(
            x -> {
              log.info("Password stored email={} userId={} org={}", email, userId, org);
              return true;
            });
  }

  @Override
  public CompletionStage<Boolean> resetRequestExists(String email, String org, UUID token) {
    return passwordResetDatasource.exists(email, org, token);
  }
}
