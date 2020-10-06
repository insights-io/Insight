package com.meemaw.auth.sso.cookie;

import com.meemaw.auth.sso.SessionCookieSecurityScheme;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirements;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@SecurityRequirements(value = {@SecurityRequirement(name = SessionCookieSecurityScheme.NAME)})
public @interface SessionCookieSecurityRequirement {}
