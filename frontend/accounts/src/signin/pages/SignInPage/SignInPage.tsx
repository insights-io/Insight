import React, { useState } from 'react';
import Head from 'next/head';
import { AccountsLayout } from 'shared/components/AccountsLayout';
import { seoTitle } from 'shared/utils/seo';
import { FaGithub, FaMicrosoft } from 'react-icons/fa';
import { useRouter } from 'next/router';
import { Block } from 'baseui/block';
import { FormControl } from 'baseui/form-control';
import Link from 'next/link';
import {
  Button,
  EmailInput,
  Label,
  Divider,
  SpacedBetween,
  UnstyledLink,
  PasswordInput,
} from '@rebrowse/elements';
import { H1 } from 'baseui/typography';
import { SIZE } from 'baseui/button';
import { StyledLink } from 'baseui/link';
import { PASSWORD_FORGOT_ROUTE, SIGNUP_ROUTE } from 'shared/constants/routes';
import { useForm } from 'react-hook-form';
import {
  EMAIL_VALIDATION,
  PASSWORD_VALIDATION,
} from 'shared/constants/form-validation';
import { UnreachableCaseError } from 'shared/utils/error';

import { createOAuth2IntegrationHrefBuilder } from './utils';

type FormData = {
  email: string;
  password: string;
};

type SignInStep = 0 | 1;

export const SignInPage = () => {
  const { query } = useRouter();
  const relativeRedirect = (query.redirect || '/') as string;
  const absoluteRedirect = global.window
    ? `${window.location.origin}${relativeRedirect}`
    : relativeRedirect;

  const oauth2IntegrationHrefBuilder = createOAuth2IntegrationHrefBuilder({
    absoluteRedirect,
  });

  const [isSubmitting, setIsSubmitting] = useState(false);
  const [step, setStep] = useState<SignInStep>(0);

  const { register, handleSubmit, errors } = useForm<FormData>({
    shouldFocusError: false,
  });

  const onSubmit = handleSubmit((_formData) => {
    setIsSubmitting(true);

    switch (step) {
      case 0:
        setTimeout(() => {
          setStep(1);
          setIsSubmitting(false);
        }, 500);
        return;
      case 1:
        throw new Error('Not implemented');
      default:
        throw new UnreachableCaseError(step);
    }
  });

  return (
    <AccountsLayout>
      {({ theme }) => (
        <>
          <Head>
            <title>{seoTitle('Sign in')}</title>
          </Head>

          <H1
            marginTop={0}
            marginBottom={theme.sizing.scale1000}
            $style={{
              fontWeight: 700,
              fontSize: theme.typography.font950.fontSize,
            }}
          >
            Sign in to Rebrowse
          </H1>

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

            {step > 0 && (
              <Block marginBottom={theme.sizing.scale1200}>
                <FormControl
                  label={
                    <SpacedBetween>
                      <Label as="span">Password</Label>
                      <Link href={PASSWORD_FORGOT_ROUTE}>
                        <StyledLink href={PASSWORD_FORGOT_ROUTE}>
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
            )}

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

          <Block>
            <UnstyledLink href={oauth2IntegrationHrefBuilder('google')}>
              <Button
                $style={{ width: '100%' }}
                startEnhancer={
                  <img
                    src="data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTYiIGhlaWdodD0iMTYiIHZpZXdCb3g9IjAgMCAxNiAxNiIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPGcgY2xpcC1wYXRoPSJ1cmwoI2NsaXAwKSI+CjxwYXRoIGQ9Ik0xNS45OTk3IDguMTg0MTdDMTUuOTk5NyA3LjY0MDM1IDE1Ljk1NDcgNy4wOTM1OSAxNS44NTg4IDYuNTU4NTlIOC4xNjAxNlY5LjYzOTI1SDEyLjU2ODhDMTIuMzg1OCAxMC42MzI4IDExLjc5OCAxMS41MTE3IDEwLjkzNzMgMTIuMDcwM1YxNC4wNjkySDEzLjU2NzVDMTUuMTEyIDEyLjY3NTggMTUuOTk5NyAxMC42MTgxIDE1Ljk5OTcgOC4xODQxN1oiIGZpbGw9IiM0Mjg1RjQiLz4KPHBhdGggZD0iTTguMTYwMTggMTYuMDAwMkMxMC4zNjE1IDE2LjAwMDIgMTIuMjE3OSAxNS4yOTE4IDEzLjU3MDUgMTQuMDY4OUwxMC45NDAzIDEyLjA3QzEwLjIwODUgMTIuNTU4IDkuMjYzODMgMTIuODM0MyA4LjE2MzE3IDEyLjgzNDNDNi4wMzM4NCAxMi44MzQzIDQuMjI4NCAxMS40MjYzIDMuNTgwNjEgOS41MzMySDAuODY2NDU1VjExLjU5MzhDMi4yNTIwMiAxNC4yOTUzIDUuMDc0MTQgMTYuMDAwMiA4LjE2MDE4IDE2LjAwMDJaIiBmaWxsPSIjMzRBODUzIi8+CjxwYXRoIGQ9Ik0zLjU3NzY3IDkuNTMzOEMzLjIzNTc4IDguNTQwMjMgMy4yMzU3OCA3LjQ2NDM1IDMuNTc3NjcgNi40NzA3OFY0LjQxMDE2SDAuODY2NTJDLTAuMjkxMTE5IDYuNjcwNjcgLTAuMjkxMTE5IDkuMzMzOTEgMC44NjY1MiAxMS41OTQ0TDMuNTc3NjcgOS41MzM4WiIgZmlsbD0iI0ZCQkMwNCIvPgo8cGF0aCBkPSJNOC4xNjAxOCAzLjE2NjQ0QzkuMzIzODEgMy4xNDg4IDEwLjQ0ODUgMy41Nzc5OCAxMS4yOTEyIDQuMzY1NzhMMTMuNjIxNSAyLjA4MTc0QzEyLjE0NTkgMC43MjM2NyAxMC4xODc1IC0wLjAyMjk3NzMgOC4xNjAxOCAwLjAwMDUzOTExMUM1LjA3NDE0IDAuMDAwNTM5MTExIDIuMjUyMDIgMS43MDU0OCAwLjg2NjQ1NSA0LjQwOTg3TDMuNTc3NjEgNi40NzA1QzQuMjIyNDEgNC41NzQ0OSA2LjAzMDg0IDMuMTY2NDQgOC4xNjAxOCAzLjE2NjQ0WiIgZmlsbD0iI0VBNDMzNSIvPgo8L2c+CjxkZWZzPgo8Y2xpcFBhdGggaWQ9ImNsaXAwIj4KPHBhdGggZD0iTTAgMEgxNlYxNkgwVjBaIiBmaWxsPSJ3aGl0ZSIvPgo8L2NsaXBQYXRoPgo8L2RlZnM+Cjwvc3ZnPgo="
                    alt="Google Logo"
                  />
                }
                kind="secondary"
                // @ts-expect-error missing typings
                tabIndex={-1}
              >
                Sign in with Google
              </Button>
            </UnstyledLink>
          </Block>

          <Block marginTop={theme.sizing.scale200}>
            <UnstyledLink href={oauth2IntegrationHrefBuilder('github')}>
              <Button
                $style={{ width: '100%' }}
                startEnhancer={<FaGithub />}
                kind="secondary"
                // @ts-expect-error missing typings
                tabIndex={-1}
              >
                Sign in with Github
              </Button>
            </UnstyledLink>
          </Block>

          <Block marginTop={theme.sizing.scale200}>
            <UnstyledLink href={oauth2IntegrationHrefBuilder('microsoft')}>
              <Button
                $style={{ width: '100%', marginBottom: theme.sizing.scale600 }}
                startEnhancer={<FaMicrosoft />}
                kind="secondary"
                // @ts-expect-error missing typings
                tabIndex={-1}
              >
                Sign in with Microsoft
              </Button>
            </UnstyledLink>
          </Block>
        </>
      )}
    </AccountsLayout>
  );
};
