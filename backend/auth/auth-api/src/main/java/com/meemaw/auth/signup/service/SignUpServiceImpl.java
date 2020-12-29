package com.meemaw.auth.signup.service;

import com.meemaw.auth.core.MailingConstants;
import com.meemaw.auth.organization.datasource.OrganizationDatasource;
import com.meemaw.auth.organization.model.CreateOrganizationParams;
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
import com.meemaw.shared.SharedConstants;
import com.meemaw.shared.logging.LoggingConstants;
import com.meemaw.shared.rest.response.Boom;
import com.meemaw.shared.sql.client.SqlPool;
import com.meemaw.shared.sql.client.SqlTransaction;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.reactive.ReactiveMailer;
import io.quarkus.qute.Template;
import io.quarkus.qute.api.ResourcePath;
import java.net.URL;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.opentracing.Traced;
import org.slf4j.MDC;

@ApplicationScoped
@Slf4j
public class SignUpServiceImpl implements SignUpService {

  @Inject SqlPool sqlPool;
  @Inject ReactiveMailer mailer;
  @Inject SignUpDatasource signUpDatasource;
  @Inject PasswordDatasource passwordDatasource;
  @Inject UserDatasource userDatasource;
  @Inject PasswordService passwordService;
  @Inject OrganizationDatasource organizationDatasource;

  @ResourcePath("sign_up/complete_sign_up")
  Template completeSignUpTemplate;

  @Override
  @Traced
  @Timed(name = "signUp", description = "A measure of how long it takes to do a sign up")
  public CompletionStage<Optional<UUID>> signUp(
      URL referrer, URL serverBaseURL, SignUpRequestDTO signUpRequestDTO) {
    MDC.put(LoggingConstants.USER_EMAIL, signUpRequestDTO.getEmail());
    log.info("[AUTH]: Sign up request for user: {}", signUpRequestDTO.getEmail());
    String hashedPassword = passwordService.hashPassword(signUpRequestDTO.getPassword());
    SignUpRequest signUpRequest =
        SignUpRequest.builder()
            .email(signUpRequestDTO.getEmail())
            .hashedPassword(hashedPassword)
            .fullName(signUpRequestDTO.getFullName())
            .company(signUpRequestDTO.getCompany())
            .phoneNumber(signUpRequestDTO.getPhoneNumber())
            .referrer(referrer)
            .build();

    return sqlPool
        .beginTransaction()
        .thenCompose(transaction -> signUp(serverBaseURL, signUpRequest, transaction));
  }

  private CompletionStage<Optional<UUID>> signUp(
      URL serverBaseURL, SignUpRequest signUpRequest, SqlTransaction transaction) {
    String email = signUpRequest.getEmail();
    return signUpDatasource
        .retrieveIsEmailTaken(email, transaction)
        .thenCompose(
            emailTaken -> {
              if (emailTaken) {
                log.info("[AUTH]: Sign up request email={} already taken", email);
                transaction.rollback();
                return CompletableFuture.completedStage(Optional.empty());
              }

              return signUpDatasource
                  .create(signUpRequest, transaction)
                  .thenCompose(
                      token ->
                          sendSignUpCompleteEmail(email, serverBaseURL, token)
                              .exceptionally(
                                  throwable -> {
                                    transaction.rollback();
                                    log.error(
                                        "[AUTH]: Failed to send sign up completion email to user: {} token: {}",
                                        email,
                                        token,
                                        throwable);
                                    throw Boom.serverError()
                                        .message("Failed to send sign up completion email")
                                        .exception(throwable);
                                  })
                              .thenApply(x -> transaction.commit())
                              .thenApply(x -> Optional.of(token)));
            });
  }

  @Traced
  private CompletionStage<Void> sendSignUpCompleteEmail(
      String email, URL serverBaseURL, UUID token) {
    String subject = "Finish your registration";
    String completeSignUpLocation =
        UriBuilder.fromUri(serverBaseURL.toString())
            .path(SignUpResource.PATH)
            .path(token.toString())
            .path("complete")
            .build()
            .toString();

    log.info(
        "[AUTH]: Sending sign up complete email={} token={} completeSignUpLocation={}",
        email,
        token,
        completeSignUpLocation);

    return completeSignUpTemplate
        .data("email", email)
        .data("token", token)
        .data("completeSignUpURL", completeSignUpLocation)
        .data("name", SharedConstants.ORGANIZATION_NAME)
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
  @Timed(
      name = "completeSignUp",
      description = "A measure of how long it takes to do complete a sign up")
  public CompletionStage<Pair<AuthUser, SignUpRequest>> completeSignUp(UUID token) {
    log.info("[AUTH]: Complete sign up attempt token: {}", token);
    return sqlPool
        .beginTransaction()
        .thenCompose(
            transaction ->
                signUpDatasource
                    .retrieve(token, transaction)
                    .thenCompose(
                        maybeSignUpRequest -> {
                          SignUpRequest signUpRequest =
                              maybeSignUpRequest.orElseThrow(
                                  () -> {
                                    transaction.rollback();
                                    return Boom.notFound().exception();
                                  });

                          return completeSignUpRequest(signUpRequest, transaction);
                        }));
  }

  private CompletionStage<Pair<AuthUser, SignUpRequest>> completeSignUpRequest(
      SignUpRequest signUpRequest, SqlTransaction transaction) {
    String email = signUpRequest.getEmail();
    UUID token = signUpRequest.getToken();
    MDC.put(LoggingConstants.USER_EMAIL, email);

    if (signUpRequest.hasExpired()) {
      transaction.rollback();
      log.info("[AUTH]: Sign up request expired for user: {} token: {}", email, token);
      throw Boom.badRequest().message("Sign up request has expired").exception();
    }

    String organizationId = Organization.identifier();
    MDC.put(LoggingConstants.ORGANIZATION_ID, organizationId);

    return organizationDatasource
        .create(
            new CreateOrganizationParams(organizationId, signUpRequest.getCompany()), transaction)
        .thenCompose(
            organization ->
                userDatasource
                    .create(
                        email,
                        signUpRequest.getFullName(),
                        organization.getId(),
                        UserRole.ADMIN,
                        signUpRequest.getPhoneNumber(),
                        transaction)
                    .thenCompose(
                        createdUser ->
                            CompletableFuture.allOf(
                                    signUpDatasource
                                        .delete(token, transaction)
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
                                      log.info(
                                          "[AUTH]: Sign up successful for user: {} token: {}",
                                          email,
                                          token);
                                      return Pair.of(createdUser, signUpRequest);
                                    })));
  }

  @Override
  @Traced
  public CompletionStage<Boolean> signUpRequestValid(UUID token) {
    return signUpDatasource
        .retrieve(token)
        .thenApply(
            maybeSignUpRequest ->
                maybeSignUpRequest.map(signUpRequest -> !signUpRequest.hasExpired()).orElse(false));
  }

  @Traced
  private CompletionStage<AuthUser> ssoSignUp(
      String email,
      String fullName,
      Function<Organization, UserRole> userRoleProvider,
      Function<SqlTransaction, CompletionStage<Organization>> organizationProvider) {
    return sqlPool
        .beginTransaction()
        .thenCompose(
            transaction ->
                organizationProvider
                    .apply(transaction)
                    .thenCompose(
                        organization ->
                            userDatasource
                                .create(
                                    email,
                                    fullName,
                                    organization.getId(),
                                    userRoleProvider.apply(organization),
                                    null,
                                    transaction)
                                .thenCompose(
                                    user -> transaction.commit().thenApply(ignored -> user))));
  }

  /**
   * Handle cases where user signed up with social SSO button, but the email was not associated with
   * any organization SSO setup. In that case, new organization is created and user is assigned an
   * ADMIN role.
   */
  @Override
  @Traced
  @Timed(name = "socialSignUp", description = "A measure of how long it takes to do social sign up")
  public CompletionStage<AuthUser> socialSignUp(String email, String fullName) {
    log.info("[AUTH]: Social sign up attempt email={}", email);
    return ssoSignUp(
        email,
        fullName,
        (organization) -> UserRole.ADMIN,
        (transaction) ->
            organizationDatasource.create(
                new CreateOrganizationParams(Organization.identifier(), null), transaction));
  }

  /**
   * Handle case where user signed up through the SSO flow, joining an existing organization with
   * SSO setup. In that case user is assigned STANDARD role.
   */
  @Override
  @Traced
  @Timed(name = "ssoSignUp", description = "A measure of how long it takes to do SSO sign up")
  public CompletionStage<AuthUser> ssoSignUp(String email, String fullName, String organizationId) {
    log.info("[AUTH]: SSO sign up attempt email={} organizationId={}", email, organizationId);
    return ssoSignUp(
        email,
        fullName,
        Organization::getDefaultRole,
        transaction ->
            organizationDatasource
                .retrieve(organizationId, transaction)
                .thenApply(
                    maybeOrganization -> {
                      if (maybeOrganization.isEmpty()) {
                        transaction.rollback();
                        throw Boom.badRequest().exception();
                      }

                      Organization organization = maybeOrganization.get();
                      if (!organization.isOpenMembership()) {
                        transaction.rollback();
                        throw Boom.badRequest()
                            .message(
                                "Organization does not support open membership. Please contact your Administrator")
                            .exception();
                      }

                      return organization;
                    }));
  }
}
