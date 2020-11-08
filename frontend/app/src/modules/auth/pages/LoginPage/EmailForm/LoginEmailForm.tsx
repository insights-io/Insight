import React, { useState } from 'react';
import { Block } from 'baseui/block';
import { FormControl } from 'baseui/form-control';
import { useForm } from 'react-hook-form';
import { AuthApi } from 'api';
import { APIError, APIErrorDataResponse } from '@insight/types';
import { Input, SpacedBetween, Button } from '@insight/elements';
import { EMAIL_VALIDATION } from 'modules/auth/validation/email';
import Link from 'next/link';
import { PASSWORD_VALIDATION } from 'modules/auth/validation/password';
import FormError from 'shared/components/FormError';
import { useStyletron } from 'baseui';
import { locationAssign } from 'shared/utils/window';
import { VERIFICATION_PAGE } from 'shared/constants/routes';

type LoginEmailFormData = {
  email: string;
  password: string;
  rememberMe: boolean;
};

type Props = {
  relativeRedirect: string;
  replace: (location: string) => void;
};

const LoginEmailForm = ({ replace, relativeRedirect }: Props) => {
  const [_css, theme] = useStyletron();
  const [formError, setFormError] = useState<APIError | undefined>();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const { register, handleSubmit, errors } = useForm<LoginEmailFormData>();

  const onSubmit = handleSubmit((formData) => {
    if (isSubmitting) {
      return;
    }
    setIsSubmitting(true);

    AuthApi.sso.session
      .login(formData.email, formData.password)
      .then((response) => {
        if (response.data === true) {
          replace(relativeRedirect);
        } else {
          replace(
            `${VERIFICATION_PAGE}?redirect=${encodeURIComponent(
              relativeRedirect
            )}`
          );
        }
      })
      .catch(async (error) => {
        const errorDTO: APIErrorDataResponse = await error.response.json();
        if (
          errorDTO.error.message === 'SSO login required' &&
          errorDTO.error.errors?.goto
        ) {
          locationAssign(errorDTO.error.errors.goto as string);
        } else {
          setFormError(errorDTO.error);
        }
      })
      .finally(() => setIsSubmitting(false));
  });

  return (
    <form onSubmit={onSubmit} noValidate>
      <Block>
        <FormControl label="Email" error={errors.email?.message}>
          <Input
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
            <SpacedBetween>
              <span>Password</span>
              <Link href="/password-forgot">
                <a>Forgot?</a>
              </Link>
            </SpacedBetween>
          }
          error={errors.password?.message}
        >
          <Input
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
