import React, { useState } from 'react';
import Head from 'next/head';
import Link from 'next/link';
import { AccountsLayout } from 'shared/components/AccountsLayout';
import { seoTitle } from 'shared/utils/seo';
import { Block } from 'baseui/block';
import { FormControl } from 'baseui/form-control';
import { Button, EmailInput, Label, SpacedBetween } from '@rebrowse/elements';
import { LOGIN_HINT_QUERY, SIGNIN_ROUTE } from 'shared/constants/routes';
import { EMAIL_VALIDATION } from 'shared/constants/form-validation';
import { useForm } from 'react-hook-form';
import { StyledLink } from 'baseui/link';
import { client, INCLUDE_CREDENTIALS } from 'sdk';
import { setFormErrors } from 'shared/utils/form';
import { CheckYourInboxPage } from 'password/pages/CheckYourInboxPage';
import type { APIErrorDataResponse } from '@rebrowse/types';

type FormData = {
  email: string;
};

export type Props = Partial<FormData>;

const TITLE = 'Forgot password?';

export const PasswordForgotPage = (defaultValues: Props) => {
  const [resetEmailSent, setResetEmailSent] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const { register, handleSubmit, errors, watch, setError } = useForm<FormData>(
    { defaultValues, shouldFocusError: false }
  );

  if (resetEmailSent) {
    return <CheckYourInboxPage />;
  }

  const onSubmit = handleSubmit((formData) => {
    setIsSubmitting(true);

    client.password
      .forgot(formData.email, INCLUDE_CREDENTIALS)
      .then(() => setResetEmailSent(true))
      .catch(async (error) => {
        const errorDTO: APIErrorDataResponse<
          Record<string, string>
        > = await error.response.json();
        setFormErrors(setError, errorDTO.error.errors);
      })
      .finally(() => setIsSubmitting(false));
  });

  const email = watch('email');
  const rememberPasswordLink = email
    ? `${SIGNIN_ROUTE}?${LOGIN_HINT_QUERY}=${email}`
    : SIGNIN_ROUTE;

  return (
    <AccountsLayout>
      {({ theme }) => (
        <>
          <Head>
            <title>{seoTitle(TITLE)}</title>
          </Head>

          <AccountsLayout.Header>{TITLE}</AccountsLayout.Header>
          <AccountsLayout.SubHeader marginBottom={theme.sizing.scale800}>
            Enter your email below and we&apos;ll send you a link to reset your
            password.
          </AccountsLayout.SubHeader>

          <form onSubmit={onSubmit} noValidate>
            <Block>
              <FormControl
                label={
                  <SpacedBetween>
                    <Label as="span">Email</Label>
                    <Link href={rememberPasswordLink}>
                      <StyledLink href={rememberPasswordLink}>
                        Remember password?
                      </StyledLink>
                    </Link>
                  </SpacedBetween>
                }
                error={errors.email?.message}
              >
                <EmailInput
                  placeholder="john.doe@gmail.com"
                  ref={(e) => {
                    e?.focus();
                    register(e, EMAIL_VALIDATION);
                  }}
                  error={Boolean(errors.email)}
                  required
                />
              </FormControl>
            </Block>

            <Button
              type="submit"
              $style={{ width: '100%' }}
              isLoading={isSubmitting}
            >
              Continue
            </Button>
          </form>
        </>
      )}
    </AccountsLayout>
  );
};
