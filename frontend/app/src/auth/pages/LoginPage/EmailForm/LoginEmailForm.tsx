import React, { useState } from 'react';
import { Block } from 'baseui/block';
import { FormControl } from 'baseui/form-control';
import { useForm } from 'react-hook-form';
import { APIError, APIErrorDataResponse } from '@rebrowse/types';
import {
  SpacedBetween,
  Button,
  PasswordInput,
  EmailInput,
  Label,
} from '@rebrowse/elements';
import { EMAIL_PLACEHOLDER } from 'shared/constants/form-placeholders';
import {
  EMAIL_VALIDATION,
  PASSWORD_VALIDATION,
} from 'shared/constants/form-validation';
import Link from 'next/link';
import { FormError } from 'shared/components/FormError';
import { useStyletron } from 'baseui';
import { locationAssign } from 'shared/utils/window';
import { VERIFICATION_PAGE } from 'shared/constants/routes';
import { client, INCLUDE_CREDENTIALS } from 'sdk';

type LoginEmailFormData = {
  email: string;
  password: string;
  rememberMe: boolean;
};

type Props = {
  relativeRedirect: string;
  replace: (location: string) => void;
};

export const LoginEmailForm = ({ replace, relativeRedirect }: Props) => {
  const [_css, theme] = useStyletron();
  const [formError, setFormError] = useState<APIError | undefined>();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const { register, handleSubmit, errors } = useForm<LoginEmailFormData>();

  const onSubmit = handleSubmit((formData) => {
    if (isSubmitting) {
      return;
    }
    setIsSubmitting(true);

    client.auth.sso.sessions
      .login(formData.email, formData.password, INCLUDE_CREDENTIALS)
      .then(({ data: loggedIn }) => {
        replace(
          loggedIn === true
            ? relativeRedirect
            : `${VERIFICATION_PAGE}?redirect=${encodeURIComponent(
                relativeRedirect
              )}`
        );
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
        <FormControl
          label={<Label as="span">Email</Label>}
          error={errors.email?.message}
        >
          <EmailInput
            placeholder={EMAIL_PLACEHOLDER}
            required
            ref={register(EMAIL_VALIDATION)}
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
          <PasswordInput
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
