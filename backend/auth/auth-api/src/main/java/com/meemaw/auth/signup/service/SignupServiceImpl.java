package com.meemaw.auth.signup.service;

import com.meemaw.auth.password.service.PasswordService;
import com.meemaw.auth.signup.datasource.SignupDatasource;
import com.meemaw.auth.signup.model.SignupRequest;
import com.meemaw.auth.signup.model.dto.SignupRequestCompleteDTO;
import com.meemaw.auth.signup.model.dto.SignupRequestDTO;
import com.meemaw.auth.user.datasource.UserDatasource;
import com.meemaw.auth.user.model.UserDTO;
import com.meemaw.auth.user.model.UserRole;
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

@ApplicationScoped
@Slf4j
public class SignupServiceImpl implements SignupService {

  @ResourcePath("signup/welcome")
  Template welcomeTemplate;

  @Inject ReactiveMailer mailer;

  @Inject PgPool pgPool;

  @Inject UserDatasource userDatasource;

  @Inject SignupDatasource signupDatasource;

  @Inject PasswordService passwordService;

  private static final String FROM_SUPPORT = "Insight Support <support@insight.com>";

  @Override
  public CompletionStage<Boolean> exists(String email, String org, UUID token) {
    return signupDatasource.exists(email, org, token);
  }

  @Override
  public CompletionStage<SignupRequestDTO> create(String email) {
    log.info("create signup request email={}", email);
    return pgPool.begin().thenCompose(transaction -> create(transaction, email));
  }

  private CompletionStage<SignupRequestDTO> create(Transaction transaction, String email) {
    return createOrganization(transaction, email)
        .thenCompose(admin -> signupDatasource.create(transaction, admin))
        .thenCompose(
            signupRequest ->
                sendWelcomeEmail(signupRequest)
                    .exceptionally(
                        throwable -> {
                          transaction.rollback();
                          log.error("Failed to send signup email={}", email, throwable);
                          throw Boom.serverError()
                              .message("Failed to send signup email")
                              .exception();
                        })
                    .thenCompose(x -> transaction.commit())
                    .thenApply(
                        x -> {
                          log.info(
                              "Signup complete email={} userId={} org={}",
                              email,
                              signupRequest.getUserId(),
                              signupRequest.getOrg());
                          return signupRequest;
                        })
                    .exceptionally(
                        throwable -> {
                          log.error(
                              "Failed to commit signup transaction email={}", email, throwable);
                          throw new DatabaseException(throwable);
                        }));
  }

  private CompletionStage<Void> sendWelcomeEmail(SignupRequestDTO signupRequest) {
    String email = signupRequest.getEmail();
    String subject = "Welcome to Insight";

    return welcomeTemplate
        .data("email", email)
        .data("orgId", signupRequest.getOrg())
        .data("token", signupRequest.getToken())
        .renderAsync()
        .thenCompose(
            html ->
                mailer
                    .send(Mail.withHtml(email, subject, html).setFrom(FROM_SUPPORT))
                    .subscribeAsCompletionStage());
  }

  @Override
  public CompletionStage<Boolean> complete(SignupRequestCompleteDTO completeSignup) {
    String email = completeSignup.getEmail();
    String org = completeSignup.getOrg();
    UUID token = completeSignup.getToken();

    return findValidSignupRequest(email, org, token)
        .thenCompose(
            validSignupRequest ->
                pgPool
                    .begin()
                    .thenCompose(
                        transaction -> {
                          UUID userId = validSignupRequest.getUserId();
                          log.info(
                              "Deleting existing signup requests email={} org={} userId={}",
                              email,
                              org,
                              userId);

                          return signupDatasource
                              .delete(transaction, email, org, userId)
                              .thenApply(
                                  isDeleted -> {
                                    if (!isDeleted) {
                                      log.info(
                                          "Failed to delete signup requests email={} org={}",
                                          email,
                                          org);
                                      throw Boom.serverError().exception();
                                    }
                                    return validSignupRequest;
                                  })
                              .thenCompose(
                                  signup -> {
                                    String password = completeSignup.getPassword();
                                    return passwordService.create(
                                        transaction, userId, email, org, password);
                                  })
                              .thenCompose(created -> transaction.commit().thenApply(x -> created));
                        }));
  }

  private CompletionStage<SignupRequestDTO> findValidSignupRequest(
      String email, String org, UUID token) {
    return signupDatasource
        .find(email, org, token)
        .thenApply(
            maybeSignup -> {
              SignupRequestDTO signup =
                  maybeSignup.orElseThrow(
                      () -> {
                        log.info(
                            "Signup request does not exist email={} org={} token={}",
                            email,
                            org,
                            token);
                        throw Boom.notFound().message("Signup request does not exist.").exception();
                      });

              if (signup.hasExpired()) {
                log.info("Signup request expired email={} org={} token={}", email, org, token);
                throw Boom.badRequest().message("Signup request expired").exception();
              }

              return signup;
            });
  }

  @Override
  public CompletionStage<UserDTO> createOrganization(String email) {
    log.info("create organization email={}", email);
    return pgPool
        .begin()
        .thenCompose(
            transaction ->
                createOrganization(transaction, email)
                    .thenCompose(user -> transaction.commit().thenApply(x -> user)));
  }

  private CompletionStage<UserDTO> createOrganization(Transaction transaction, String email) {
    return userDatasource
        .createOrganization(transaction, new SignupRequest(email))
        .thenCompose(
            org ->
                userDatasource
                    .createUser(transaction, org)
                    .thenApply(
                        signupRequest ->
                            new UserDTO(
                                signupRequest.userId(),
                                signupRequest.email(),
                                UserRole.ADMIN,
                                signupRequest.org())));
  }
}
