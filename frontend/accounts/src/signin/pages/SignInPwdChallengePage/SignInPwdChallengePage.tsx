import React, { useState } from 'react';
import Head from 'next/head';
import { AccountsLayout } from 'shared/components/AccountsLayout';
import { seoTitle } from 'shared/utils/seo';
import { Block } from 'baseui/block';
import { FormControl } from 'baseui/form-control';
import Link from 'next/link';
import {
  Button,
  Label,
  Divider,
  SpacedBetween,
  UnstyledLink,
  PasswordInput,
  Flex,
  expandBorderRadius,
} from '@rebrowse/elements';
import { SIZE } from 'baseui/button';
import { StyledLink } from 'baseui/link';
import {
  LOGIN_HINT_QUERY,
  PASSWORD_FORGOT_ROUTE,
  REDIRECT_QUERY,
  SIGNIN_MFA_CHALLENGE_ROUTE,
  SIGNIN_ROUTE,
  SIGNUP_ROUTE,
} from 'shared/constants/routes';
import { useForm } from 'react-hook-form';
import { PASSWORD_VALIDATION } from 'shared/constants/form-validation';
import { ChevronDown } from 'baseui/icon';
import { client, INCLUDE_CREDENTIALS } from 'sdk';
import { APIError, APIErrorDataResponse } from '@rebrowse/types';
import { FormError } from 'shared/components/FormError';
import { useRouter } from 'next/router';
import { SsoProviders } from 'signin/components/SsoProviders';
import { locationAssign } from 'shared/utils/window';

export type Props = {
  email: string;
  redirect: string;
};

type FormData = {
  password: string;
};

const TITLE = "Verify it's you";

export const SignInPwdChallengePage = ({ email, redirect }: Props) => {
  const router = useRouter();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [apiError, setApiError] = useState<APIError>();

  const { register, handleSubmit, errors, setError } = useForm<FormData>({
    shouldFocusError: false,
  });

  const onSubmit = handleSubmit((formData) => {
    setIsSubmitting(true);
    setApiError(undefined);
    client.accounts
      .completePwdChallenge({ ...formData, email }, INCLUDE_CREDENTIALS)
      .then((response) => {
        if (response.data.action === 'SUCCESS') {
          locationAssign(response.data.location);
        } else {
          router.push(SIGNIN_MFA_CHALLENGE_ROUTE);
        }
      })
      .catch(async (error) => {
        const errorDTO: APIErrorDataResponse = await error.response.json();
        setError('password', { message: errorDTO.error.message });
      })
      .finally(() => setIsSubmitting(false));
  });

  const signInRoute = `${SIGNIN_ROUTE}?${REDIRECT_QUERY}=${redirect}`;
  const signUpRoute = `${SIGNUP_ROUTE}?${LOGIN_HINT_QUERY}=${email}`;
  const passwordForgotRoute = `${PASSWORD_FORGOT_ROUTE}?${LOGIN_HINT_QUERY}=${email}`;

  return (
    <AccountsLayout>
      {({ theme }) => (
        <>
          <Head>
            <title>{seoTitle(TITLE)}</title>
          </Head>

          <Flex justifyContent="center">
            <AccountsLayout.Header>{TITLE}</AccountsLayout.Header>
          </Flex>

          <Flex justifyContent="center" marginBottom={theme.sizing.scale1000}>
            <Link href={signInRoute}>
              <UnstyledLink href={signInRoute}>
                <Button
                  kind="secondary"
                  size="compact"
                  endEnhancer={<ChevronDown />}
                  $style={{ ...expandBorderRadius(theme.sizing.scale800) }}
                  // @ts-expect-error missing typings
                  tabIndex={-1}
                >
                  {email}
                </Button>
              </UnstyledLink>
            </Link>
          </Flex>

          <form noValidate onSubmit={onSubmit}>
            <Block marginBottom={theme.sizing.scale1200}>
              <FormControl
                label={
                  <SpacedBetween>
                    <Label as="span">Password</Label>
                    <Link href={passwordForgotRoute}>
                      <StyledLink href={passwordForgotRoute}>
                        Forgot?
                      </StyledLink>
                    </Link>
                  </SpacedBetween>
                }
                error={errors.password?.message}
              >
                <PasswordInput
                  autoComplete="current-password"
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

            {apiError && <FormError error={apiError} />}
          </form>
          <SpacedBetween marginTop={theme.sizing.scale600}>
            <Link href={signUpRoute}>
              <StyledLink href={signUpRoute}>
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
