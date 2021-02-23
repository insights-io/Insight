package com.rebrowse.beacon.core;

import com.rebrowse.shared.SharedConstants;
import javax.ws.rs.core.Application;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.servers.Server;

@OpenAPIDefinition(
    info =
        @Info(
            title = App.TITLE,
            version = App.VERSION,
            contact =
                @Contact(
                    name = "Support",
                    url = "mailto:support@" + SharedConstants.REBROWSE_STAGING_DOMAIN,
                    email = "support@" + SharedConstants.REBROWSE_STAGING_DOMAIN)),
    servers = @Server(url = App.SERVER))
public class App extends Application {

  public static final String TITLE = "Beacon API";
  public static final String VERSION = "1.0.0";
  public static final String SERVER = "http://localhost:8081";
}
