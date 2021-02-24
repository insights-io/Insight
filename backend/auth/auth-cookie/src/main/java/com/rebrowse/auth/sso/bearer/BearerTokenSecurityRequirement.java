package com.rebrowse.auth.sso.bearer;

import com.rebrowse.auth.sso.BearerTokenSecurityScheme;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirements;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@SecurityRequirements(value = {@SecurityRequirement(name = BearerTokenSecurityScheme.NAME)})
public @interface BearerTokenSecurityRequirement {}
