package com.meemaw.rec.core;

import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Info;

import javax.ws.rs.core.Application;

@OpenAPIDefinition(
    info = @Info(
        title = "Recording API",
        version = "1.0.0"
    ))
public class App extends Application {

}
