package com.rebrowse.auth.organization.model.validation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.rebrowse.auth.organization.model.Organization;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@NotBlank(message = "Required")
@Size(
    min = Organization.ID_LENGTH,
    max = Organization.ID_LENGTH,
    message = "Has to be " + Organization.ID_LENGTH + " characters long")
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.PARAMETER})
@Retention(RUNTIME)
@Constraint(validatedBy = {})
public @interface OrganizationId {

  String message() default "Organization ID is invalid";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
