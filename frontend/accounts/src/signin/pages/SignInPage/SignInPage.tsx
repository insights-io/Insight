import React, { useState } from 'react';
import Head from 'next/head';
import { AccountsLayout } from 'shared/components/AccountsLayout';
import { seoTitle } from 'shared/utils/seo';
import { Block } from 'baseui/block';
import { FormControl } from 'baseui/form-control';
import Link from 'next/link';
import {
  Button,
  EmailInput,
  Label,
  Divider,
  SpacedBetween,
} from '@rebrowse/elements';
import { SIZE } from 'baseui/button';
import { StyledLink } from 'baseui/link';
import {
  SIGNIN_PWD_CHALLENGE_ROUTE,
  SIGNUP_ROUTE,
} from 'shared/constants/routes';
import { useForm } from 'react-hook-form';
import { EMAIL_VALIDATION } from 'shared/constants/form-validation';
import { client, INCLUDE_CREDENTIALS } from 'sdk';
import { useRouter } from 'next/router';
import { SsoProviders } from 'signin/components/SsoProviders';
import { locationAssign } from 'shared/utils/window';

export type Props = {
  email?: string;
  redirect: string;
};

type FormData = {
  email: string;
};

export const SignInPage = ({ redirect, ...defaultValues }: Props) => {
  const router = useRouter();
  const [isSubmitting, setIsSubmitting] = useState(false);

  const { register, handleSubmit, errors } = useForm<FormData>({
    shouldFocusError: false,
    defaultValues,
  });

  const onSubmit = handleSubmit((formData) => {
    setIsSubmitting(true);
    client.accounts
      .chooseAccount({ ...formData, redirect }, INCLUDE_CREDENTIALS)
      .then((response) => {
        if (response.data.action === 'PWD_CHALLENGE') {
          router.push(SIGNIN_PWD_CHALLENGE_ROUTE);
        } else {
          locationAssign(response.data.location);
        }
      })
      .finally(() => setIsSubmitting(false));
  });

  return (
    <AccountsLayout>
      {({ theme }) => (
        <>
          <Head>
            <title>{seoTitle('Sign in')}</title>
          </Head>
          <AccountsLayout.Header>Sign in to Rebrowse</AccountsLayout.Header>
          <form noValidate onSubmit={onSubmit}>
            <Block>
              <FormControl
                label={<Label as="span">Email</Label>}
                error={errors.email?.message}
              >
                <EmailInput
                  required
                  placeholder="john.doe@gmail.com"
                  error={Boolean(errors.email)}
                  ref={(e) => {
                    e?.focus();
                    register(e, EMAIL_VALIDATION);
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

          <SpacedBetween marginTop={theme.sizing.scale600}>
            <Link href={SIGNUP_ROUTE}>
              <StyledLink href={SIGNUP_ROUTE}>
                <Button
                  kind="minimal"
                  size={SIZE.compact}
                  // @ts-expect-error missing typings
                  tabIndex={-1}
                >
                  Create a free account
                </Button>
              </StyledLink>
            </Link>

            <StyledLink href="/join-team">
              <Button
                kind="minimal"
                size={SIZE.compact}
                // @ts-expect-error missing typings
                tabIndex={-1}
              >
                Join an existing team
              </Button>
            </StyledLink>
          </SpacedBetween>

          <Divider>
            <Divider.Line />
            <Divider.Or />
            <Divider.Line />
          </Divider>

          <SsoProviders redirect={redirect} theme={theme} />
        </>
      )}
    </AccountsLayout>
  );
};
