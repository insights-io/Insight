package com.meemaw.auth.core;

import javax.ws.rs.core.Application;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.servers.Server;

@OpenAPIDefinition(
    info = @Info(
        title = "Auth API",
        version = "1.0.0"
    ),
    servers = @Server(
        url = "http://localhost:8080"
    ),
    security = @SecurityRequirement(
        name = "Cookie Auth",
        scopes = "cookie"
    )
)
public class App extends Application {

}
