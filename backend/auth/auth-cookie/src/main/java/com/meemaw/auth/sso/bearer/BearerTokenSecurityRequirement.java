package com.meemaw.auth.sso.bearer;

import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirements;

import com.meemaw.auth.sso.BearerTokenSecurityScheme;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@SecurityRequirements(value = {@SecurityRequirement(name = BearerTokenSecurityScheme.NAME)})
public @interface BearerTokenSecurityRequirement {}
