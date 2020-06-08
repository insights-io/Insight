package com.meemaw.auth.signup.service;

import com.meemaw.auth.core.MailingConstants;
import com.meemaw.auth.organization.datasource.OrganizationDatasource;
import com.meemaw.auth.organization.model.Organization;
import com.meemaw.auth.password.datasource.PasswordDatasource;
import com.meemaw.auth.password.service.PasswordService;
import com.meemaw.auth.signup.datasource.SignUpDatasource;
import com.meemaw.auth.signup.model.SignUpRequest;
import com.meemaw.auth.signup.model.dto.SignUpRequestDTO;
import com.meemaw.auth.signup.resource.v1.SignUpResource;
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
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

@ApplicationScoped
@Slf4j
public class SignUpServiceImpl implements SignUpService {

  @Inject PgPool pgPool;
  @Inject ReactiveMailer mailer;
  @Inject SignUpDatasource signUpDatasource;
  @Inject PasswordDatasource passwordDatasource;
  @Inject UserDatasource userDatasource;
  @Inject PasswordService passwordService;
  @Inject OrganizationDatasource organizationDatasource;

  @ResourcePath("sign_up/complete_sign_up")
  Template completeSignUpTemplate;

  @Override
  public CompletionStage<Optional<UUID>> signUp(
      String referer, String serverBaseURL, SignUpRequestDTO signUpRequestDTO) {
    log.info("Sign up request");
    String hashedPassword = passwordService.hashPassword(signUpRequestDTO.getPassword());
    SignUpRequest signUpRequest =
        SignUpRequest.builder()
            .email(signUpRequestDTO.getEmail())
            .hashedPassword(hashedPassword)
            .fullName(signUpRequestDTO.getFullName())
            .company(signUpRequestDTO.getCompany())
            .phoneNumber(signUpRequestDTO.getPhoneNumber())
            .referer(referer)
            .build();

    return pgPool
        .begin()
        .thenCompose(transaction -> signUp(serverBaseURL, signUpRequest, transaction));
  }

  private CompletionStage<Optional<UUID>> signUp(
      String serverBaseURL, SignUpRequest signUpRequest, Transaction transaction) {

    return signUpDatasource
        .selectIsEmailTaken(signUpRequest.getEmail(), transaction)
        .thenCompose(
            emailTaken -> {
              if (emailTaken) {
                log.info("Sign up request email taken");
                return CompletableFuture.completedStage(Optional.empty());
              }

              return signUpDatasource
                  .createSignUpRequest(signUpRequest, transaction)
                  .thenCompose(
                      token ->
                          sendSignUpCompleteEmail(signUpRequest.getEmail(), serverBaseURL, token)
                              .exceptionally(
                                  throwable -> {
                                    transaction.rollback();
                                    log.error("Failed to send sign up completion email", throwable);
                                    throw Boom.serverError()
                                        .message("Failed to send sign up completion email")
                                        .exception(throwable);
                                  })
                              .thenApply(x -> transaction.commit())
                              .thenApply(x -> Optional.of(token)));
            });
  }

  private CompletionStage<Void> sendSignUpCompleteEmail(
      String email, String serverBaseURL, UUID token) {
    String subject = "Finish your registration";
    String completeSignUpURL =
        String.join("/", serverBaseURL + SignUpResource.PATH, token.toString(), "complete");

    return completeSignUpTemplate
        .data("email", email)
        .data("token", token)
        .data("completeSignUpURL", completeSignUpURL)
        .renderAsync()
        .thenCompose(
            html ->
                mailer
                    .send(
                        Mail.withHtml(email, subject, html).setFrom(MailingConstants.FROM_SUPPORT))
                    .subscribeAsCompletionStage());
  }

  @Override
  public CompletionStage<Pair<AuthUser, SignUpRequest>> completeSignUp(UUID token) {
    return pgPool
        .begin()
        .thenCompose(
            transaction ->
                signUpDatasource
                    .findSignUpRequest(token, transaction)
                    .thenCompose(
                        maybeSignUpRequest ->
                            completeSignUpRequest(
                                maybeSignUpRequest.orElseThrow(() -> Boom.notFound().exception()),
                                transaction)));
  }

  private CompletionStage<Pair<AuthUser, SignUpRequest>> completeSignUpRequest(
      SignUpRequest signUpRequest, Transaction transaction) {
    if (signUpRequest.hasExpired()) {
      log.info("Sign up request has expired");
      throw Boom.badRequest().message("Sign up request has expired").exception();
    }
    String email = signUpRequest.getEmail();
    String organizationId = Organization.identifier();
    UserRole userRole = UserRole.ADMIN;

    return organizationDatasource
        .createOrganization(organizationId, signUpRequest.getCompany(), transaction)
        .thenCompose(
            organization ->
                userDatasource
                    .createUser(
                        email,
                        signUpRequest.getFullName(),
                        organization.getId(),
                        userRole,
                        transaction)
                    .thenCompose(
                        createdUser ->
                            CompletableFuture.allOf(
                                    signUpDatasource
                                        .deleteSignUpRequest(signUpRequest.getToken(), transaction)
                                        .toCompletableFuture(),
                                    passwordDatasource
                                        .storePassword(
                                            createdUser.getId(),
                                            signUpRequest.getHashedPassword(),
                                            transaction)
                                        .toCompletableFuture())
                                .thenCompose(x -> transaction.commit())
                                .thenApply(
                                    x -> {
                                      log.info("Sign up completed");
                                      return Pair.of(createdUser, signUpRequest);
                                    })));
  }

  @Override
  public CompletionStage<Boolean> signUpRequestValid(UUID token) {
    return signUpDatasource
        .findSignUpRequest(token)
        .thenApply(
            maybeSignUpRequest ->
                maybeSignUpRequest.map(signUpRequest -> !signUpRequest.hasExpired()).orElse(false));
  }

  @Override
  public CompletionStage<AuthUser> socialSignUp(String email, String fullName) {
    log.info("Social sign up request");
    return pgPool
        .begin()
        .thenCompose(
            transaction ->
                organizationDatasource
                    .createOrganization(Organization.identifier(), null, transaction)
                    .thenCompose(
                        organization ->
                            userDatasource
                                .createUser(
                                    email,
                                    fullName,
                                    organization.getId(),
                                    UserRole.ADMIN,
                                    transaction)
                                .thenCompose(
                                    user -> transaction.commit().thenApply(ignored -> user))));
  }
}
