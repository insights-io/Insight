import React, { useState } from 'react';
import Head from 'next/head';
import { AccountsLayout } from 'shared/components/AccountsLayout';
import { seoTitle } from 'shared/utils/seo';
import { Block } from 'baseui/block';
import { FormControl } from 'baseui/form-control';
import { Button, Label, PasswordInput } from '@rebrowse/elements';
import { PASSWORD_VALIDATION } from 'shared/constants/form-validation';
import { useForm } from 'react-hook-form';
import { client, INCLUDE_CREDENTIALS } from 'sdk';
import { setFormErrors } from 'shared/utils/form';
import type { APIErrorDataResponse } from '@rebrowse/types';
import { useRouter } from 'next/router';
import { SIGNIN_MFA_CHALLENGE_ROUTE } from 'shared/constants/routes';
import { locationAssign } from 'shared/utils/window';

type FormData = {
  password: string;
};

type Props = {
  token: string;
};

const TITLE = 'Reset your password';

export const PasswordResetPage = ({ token }: Props) => {
  const router = useRouter();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const { register, handleSubmit, errors, setError } = useForm<FormData>({
    shouldFocusError: false,
  });

  const onSubmit = handleSubmit(({ password }) => {
    setIsSubmitting(true);
    client.password
      .reset({ token, password }, INCLUDE_CREDENTIALS)
      .then((response) => {
        if (response.data.action === 'SUCCESS') {
          locationAssign(response.data.location);
        } else {
          router.push(SIGNIN_MFA_CHALLENGE_ROUTE);
        }
      })
      .catch(async (error) => {
        const errorDTO: APIErrorDataResponse<
          Record<string, string>
        > = await error.response.json();
        setFormErrors(setError, errorDTO.error.errors);
      })
      .finally(() => setIsSubmitting(false));
  });

  return (
    <AccountsLayout>
      {() => (
        <>
          <Head>
            <title>{seoTitle(TITLE)}</title>
          </Head>

          <AccountsLayout.Header>{TITLE}</AccountsLayout.Header>

          <form onSubmit={onSubmit} noValidate>
            <Block>
              <FormControl
                label={<Label>Password</Label>}
                error={errors.password?.message}
              >
                <PasswordInput
                  autoComplete="new-password"
                  error={Boolean(errors.password)}
                  ref={(e) => {
                    e?.focus();
                    register(e, PASSWORD_VALIDATION);
                  }}
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
