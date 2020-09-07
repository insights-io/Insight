import React, { useState } from 'react';
import { Block } from 'baseui/block';
import { FormControl } from 'baseui/form-control';
import { Input } from 'baseui/input';
import { useForm } from 'react-hook-form';
import { AuthApi } from 'api';
import { APIError, APIErrorDataResponse } from '@insight/types';
import { createInputOverrides } from 'shared/styles/input';
import { EMAIL_VALIDATION } from 'modules/auth/validation/email';
import Link from 'next/link';
import { PASSWORD_VALIDATION } from 'modules/auth/validation/password';
import { Button } from 'baseui/button';
import FormError from 'shared/components/FormError';
import { useStyletron } from 'baseui';

type LoginEmailFormData = {
  email: string;
  password: string;
  rememberMe: boolean;
};

type Props = {
  redirect: string;
  encodedRedirect: string;
  replace: (location: string) => void;
};

const LoginEmailForm = ({ replace, redirect, encodedRedirect }: Props) => {
  const [_css, theme] = useStyletron();
  const [formError, setFormError] = useState<APIError | undefined>();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const { register, handleSubmit, errors } = useForm<LoginEmailFormData>();
  const inputOverrides = createInputOverrides(theme);

  const onSubmit = handleSubmit((formData) => {
    if (isSubmitting) {
      return;
    }
    setIsSubmitting(true);

    AuthApi.sso
      .login(formData.email, formData.password)
      .then((response) => {
        if (response.data === true) {
          replace(redirect);
        } else {
          replace(`/login/verification?redirect=${encodedRedirect}`);
        }
      })
      .catch(async (error) => {
        const errorDTO: APIErrorDataResponse = await error.response.json();
        setFormError(errorDTO.error);
      })
      .finally(() => setIsSubmitting(false));
  });

  return (
    <form onSubmit={onSubmit} noValidate>
      <Block>
        <FormControl label="Email" error={errors.email?.message}>
          <Input
            overrides={inputOverrides}
            id="email"
            name="email"
            type="email"
            placeholder="Email"
            required
            inputRef={register(EMAIL_VALIDATION)}
            error={Boolean(errors.email)}
          />
        </FormControl>
      </Block>
      <Block marginBottom={theme.sizing.scale1200}>
        <FormControl
          label={
            <Block display="flex" justifyContent="space-between">
              <span>Password</span>
              <Link href="/password-forgot">
                <a>Forgot?</a>
              </Link>
            </Block>
          }
          error={errors.password?.message}
        >
          <Input
            overrides={inputOverrides}
            id="password"
            name="password"
            type="password"
            placeholder="Password"
            ref={register}
            inputRef={register(PASSWORD_VALIDATION)}
            error={Boolean(errors.password)}
          />
        </FormControl>
      </Block>

      <Button type="submit" $style={{ width: '100%' }} isLoading={isSubmitting}>
        Sign in
      </Button>
      {formError && <FormError error={formError} />}
    </form>
  );
};

export default React.memo(LoginEmailForm);
