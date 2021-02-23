package com.rebrowse.shared.validation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@NotBlank(message = "Required")
@Size(min = 8, message = "Password must be at least 8 characters long")
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.PARAMETER})
@Retention(RUNTIME)
@Constraint(validatedBy = {})
public @interface Password {

  /** @return error message */
  String message() default "Password is invalid";

  /** @return groups */
  Class<?>[] groups() default {};

  /** @return payload */
  Class<? extends Payload>[] payload() default {};
}
