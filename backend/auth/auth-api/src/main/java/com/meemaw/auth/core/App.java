package com.meemaw.auth.core;

import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeIn;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.servers.Server;

import com.meemaw.auth.sso.BearerTokenSecurityScheme;
import com.meemaw.auth.sso.SessionCookieSecurityScheme;
import com.meemaw.auth.sso.session.model.SsoSession;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.HttpHeaders;

@OpenAPIDefinition(
    info =
        @Info(
            title = App.TITLE,
            version = App.VERSION,
            description = App.DESCRIPTION,
            contact =
                @Contact(
                    name = "Support",
                    url = "mailto:support@rebrowse.io",
                    email = "support@rebrowse.io")),
    servers = @Server(url = App.SERVER))
@SecurityScheme(
    securitySchemeName = BearerTokenSecurityScheme.NAME,
    description = BearerTokenSecurityScheme.DESCRIPTION,
    type = SecuritySchemeType.APIKEY,
    in = SecuritySchemeIn.HEADER,
    apiKeyName = HttpHeaders.AUTHORIZATION)
@SecurityScheme(
    securitySchemeName = SessionCookieSecurityScheme.NAME,
    description = SessionCookieSecurityScheme.DESCRIPTION,
    type = SecuritySchemeType.APIKEY,
    in = SecuritySchemeIn.COOKIE,
    apiKeyName = SsoSession.COOKIE_NAME)
public class App extends Application {

  public static final String TITLE = "Auth API";
  public static final String VERSION = "1.0.0";
  public static final String SERVER = "http://localhost:8080";
  public static final String DESCRIPTION =
      "Auth API is responsible for everything related to authentication and authorization.";
}
