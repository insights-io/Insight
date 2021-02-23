package com.rebrowse.auth.core;

import com.rebrowse.auth.accounts.model.challenge.AuthorizationPwdChallengeSession;
import com.rebrowse.auth.sso.BearerTokenSecurityScheme;
import com.rebrowse.auth.sso.MfaChallengeSessionCookieSecurityScheme;
import com.rebrowse.auth.sso.SsoSessionCookieSecurityScheme;
import com.rebrowse.auth.sso.session.model.SsoSession;
import com.rebrowse.shared.SharedConstants;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.HttpHeaders;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeIn;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.servers.Server;

@OpenAPIDefinition(
    info =
        @Info(
            title = App.TITLE,
            version = App.VERSION,
            description = App.DESCRIPTION,
            contact =
                @Contact(
                    name = "Support",
                    url = "mailto:support@" + SharedConstants.REBROWSE_STAGING_DOMAIN,
                    email = "support@" + SharedConstants.REBROWSE_STAGING_DOMAIN)),
    servers = @Server(url = App.SERVER))
@SecurityScheme(
    securitySchemeName = BearerTokenSecurityScheme.NAME,
    description = BearerTokenSecurityScheme.DESCRIPTION,
    type = SecuritySchemeType.APIKEY,
    in = SecuritySchemeIn.HEADER,
    apiKeyName = HttpHeaders.AUTHORIZATION)
@SecurityScheme(
    securitySchemeName = SsoSessionCookieSecurityScheme.NAME,
    description = SsoSessionCookieSecurityScheme.DESCRIPTION,
    type = SecuritySchemeType.APIKEY,
    in = SecuritySchemeIn.COOKIE,
    apiKeyName = SsoSession.COOKIE_NAME)
@SecurityScheme(
    securitySchemeName = MfaChallengeSessionCookieSecurityScheme.NAME,
    description = MfaChallengeSessionCookieSecurityScheme.DESCRIPTION,
    type = SecuritySchemeType.APIKEY,
    in = SecuritySchemeIn.COOKIE,
    apiKeyName = AuthorizationPwdChallengeSession.COOKIE_NAME)
public class App extends Application {

  public static final String TITLE = "Auth API";
  public static final String VERSION = "1.0.0";
  public static final String SERVER = "http://localhost:8080";
  public static final String DESCRIPTION =
      "Auth API is responsible for everything related to authentication and authorization.";
}
