package com.meemaw.auth.sso.bearer;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meemaw.auth.sso.AbstractAuthDynamicFeature;
import com.meemaw.auth.sso.session.model.InsightSecurityContext;
import com.meemaw.auth.user.model.dto.UserDTO;
import com.meemaw.shared.context.RequestContextUtils;
import com.meemaw.shared.rest.response.Boom;
import io.opentracing.Span;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.opentracing.Traced;

@Slf4j
@Provider
public class BearerTokenAuthDynamicFeature extends AbstractAuthDynamicFeature<BearerTokenAuth> {

  private static final Pattern BEARER_PATTERN =
      Pattern.compile("^Bearer *([^ ]+) *$", Pattern.CASE_INSENSITIVE);

  @Inject ObjectMapper objectMapper;

  @ConfigProperty(name = "authorization.issuer")
  String issuer;

  @ConfigProperty(name = "authorization.signing.secret")
  String signingSecret;

  JWTVerifier jwtVerifier;

  @PostConstruct
  public void init() {
    Algorithm algorithm = Algorithm.HMAC256(signingSecret);
    jwtVerifier = JWT.require(algorithm).withIssuer(issuer).build();
  }

  @Override
  public Class<BearerTokenAuth> getAnnotation() {
    return BearerTokenAuth.class;
  }

  @Override
  public ContainerRequestFilter authFilter() {
    return new BearerTokenAuthFilter();
  }

  @Priority(Priorities.AUTHENTICATION)
  private class BearerTokenAuthFilter implements ContainerRequestFilter {

    @Override
    @Traced(operationName = "BearerTokenAuthDynamicFeature.filter")
    public void filter(ContainerRequestContext context) {
      Span span = tracer.activeSpan();
      String authorization = context.getHeaderString(HttpHeaders.AUTHORIZATION);
      if (authorization == null) {
        log.debug("[AUTH]: Missing authorization header");
        span.log("[BearerTokenAuth]: Missing authorization header");
        throw Boom.status(Status.UNAUTHORIZED).exception();
      }
      Matcher matcher = BEARER_PATTERN.matcher(authorization);
      if (!matcher.matches()) {
        log.debug("[AUTH]: Malformed authorization header");
        span.setTag(HttpHeaders.AUTHORIZATION, authorization);
        span.log("[BearerTokenAuth]: Malformed authorization header");
        throw Boom.status(Status.UNAUTHORIZED).exception();
      }

      String token = matcher.group(1);
      try {
        DecodedJWT jwt = jwtVerifier.verify(token);
        UserDTO user = objectMapper.readValue(jwt.getPayload(), UserDTO.class);
        setUserContext(span, user);
        boolean isSecure = RequestContextUtils.getServerBaseURL(context).startsWith("https");
        context.setSecurityContext(new InsightSecurityContext(user, isSecure));
        principal.user(user);
        log.debug("[AUTH]: Successfully authenticated user={}", user.getId());
      } catch (JWTVerificationException | JsonProcessingException ex) {
        throw Boom.status(Status.UNAUTHORIZED).message(ex.getMessage()).exception(ex);
      }
    }
  }
}
