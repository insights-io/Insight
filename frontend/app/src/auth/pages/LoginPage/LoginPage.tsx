import React, { useState } from 'react';
import Head from 'next/head';
import { useStyletron } from 'baseui';
import { Button, SIZE } from 'baseui/button';
import { useRouter } from 'next/router';
import Divider from 'shared/components/Divider';
import { TRY_BASE_URL } from 'shared/config';
import { AuthPageLayout } from 'auth/components/PageLayout';
import { FaGithub, FaMicrosoft } from 'react-icons/fa';
import { SsoButton } from 'auth/components/SsoButton';
import { FILL, Tab, Tabs } from 'baseui/tabs-motion';
import { Flex, UnstyledLink } from '@rebrowse/elements';
import { FormError } from 'shared/components/FormError';

import { createOAuth2IntegrationHrefBuilder } from './utils';
import LoginEmailForm from './EmailForm';
import { LoginMethod } from './types';
import LoginSamlSsoForm from './SamlSsoForm';

export const LoginPage = () => {
  const [activeMethod, setActiveMethod] = useState<LoginMethod>('email');
  const { query, replace } = useRouter();
  const [_css, theme] = useStyletron();

  const maybeOAuthError = query.oauthError;
  const relativeRedirect = (query.redirect || '/') as string;
  const absoluteRedirect = `${window.location.origin}${relativeRedirect}`;
  const oauth2IntegrationHrefBuilder = createOAuth2IntegrationHrefBuilder({
    absoluteRedirect,
  });

  return (
    <AuthPageLayout>
      <Head>
        <title>Sign in</title>
      </Head>

      <SsoButton
        href={oauth2IntegrationHrefBuilder('google')}
        icon={
          <img
            src="data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTYiIGhlaWdodD0iMTYiIHZpZXdCb3g9IjAgMCAxNiAxNiIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPGcgY2xpcC1wYXRoPSJ1cmwoI2NsaXAwKSI+CjxwYXRoIGQ9Ik0xNS45OTk3IDguMTg0MTdDMTUuOTk5NyA3LjY0MDM1IDE1Ljk1NDcgNy4wOTM1OSAxNS44NTg4IDYuNTU4NTlIOC4xNjAxNlY5LjYzOTI1SDEyLjU2ODhDMTIuMzg1OCAxMC42MzI4IDExLjc5OCAxMS41MTE3IDEwLjkzNzMgMTIuMDcwM1YxNC4wNjkySDEzLjU2NzVDMTUuMTEyIDEyLjY3NTggMTUuOTk5NyAxMC42MTgxIDE1Ljk5OTcgOC4xODQxN1oiIGZpbGw9IiM0Mjg1RjQiLz4KPHBhdGggZD0iTTguMTYwMTggMTYuMDAwMkMxMC4zNjE1IDE2LjAwMDIgMTIuMjE3OSAxNS4yOTE4IDEzLjU3MDUgMTQuMDY4OUwxMC45NDAzIDEyLjA3QzEwLjIwODUgMTIuNTU4IDkuMjYzODMgMTIuODM0MyA4LjE2MzE3IDEyLjgzNDNDNi4wMzM4NCAxMi44MzQzIDQuMjI4NCAxMS40MjYzIDMuNTgwNjEgOS41MzMySDAuODY2NDU1VjExLjU5MzhDMi4yNTIwMiAxNC4yOTUzIDUuMDc0MTQgMTYuMDAwMiA4LjE2MDE4IDE2LjAwMDJaIiBmaWxsPSIjMzRBODUzIi8+CjxwYXRoIGQ9Ik0zLjU3NzY3IDkuNTMzOEMzLjIzNTc4IDguNTQwMjMgMy4yMzU3OCA3LjQ2NDM1IDMuNTc3NjcgNi40NzA3OFY0LjQxMDE2SDAuODY2NTJDLTAuMjkxMTE5IDYuNjcwNjcgLTAuMjkxMTE5IDkuMzMzOTEgMC44NjY1MiAxMS41OTQ0TDMuNTc3NjcgOS41MzM4WiIgZmlsbD0iI0ZCQkMwNCIvPgo8cGF0aCBkPSJNOC4xNjAxOCAzLjE2NjQ0QzkuMzIzODEgMy4xNDg4IDEwLjQ0ODUgMy41Nzc5OCAxMS4yOTEyIDQuMzY1NzhMMTMuNjIxNSAyLjA4MTc0QzEyLjE0NTkgMC43MjM2NyAxMC4xODc1IC0wLjAyMjk3NzMgOC4xNjAxOCAwLjAwMDUzOTExMUM1LjA3NDE0IDAuMDAwNTM5MTExIDIuMjUyMDIgMS43MDU0OCAwLjg2NjQ1NSA0LjQwOTg3TDMuNTc3NjEgNi40NzA1QzQuMjIyNDEgNC41NzQ0OSA2LjAzMDg0IDMuMTY2NDQgOC4xNjAxOCAzLjE2NjQ0WiIgZmlsbD0iI0VBNDMzNSIvPgo8L2c+CjxkZWZzPgo8Y2xpcFBhdGggaWQ9ImNsaXAwIj4KPHBhdGggZD0iTTAgMEgxNlYxNkgwVjBaIiBmaWxsPSJ3aGl0ZSIvPgo8L2NsaXBQYXRoPgo8L2RlZnM+Cjwvc3ZnPgo="
            alt="Google Logo"
          />
        }
        theme={theme}
      >
        Sign in with Google
      </SsoButton>

      <SsoButton
        href={oauth2IntegrationHrefBuilder('github')}
        icon={<FaGithub />}
        theme={theme}
      >
        Sign in with Github
      </SsoButton>

      <SsoButton
        href={oauth2IntegrationHrefBuilder('microsoft')}
        icon={<FaMicrosoft />}
        theme={theme}
      >
        Sign in with Microsoft
      </SsoButton>

      <Tabs
        overrides={{ Root: { style: { marginTop: theme.sizing.scale600 } } }}
        activeKey={activeMethod}
        onChange={(params) => setActiveMethod(params.activeKey as LoginMethod)}
        activateOnFocus
        fill={FILL.fixed}
      >
        <Tab title="Email" key="email">
          <LoginEmailForm
            relativeRedirect={relativeRedirect}
            replace={replace}
          />
        </Tab>
        <Tab title="SSO" key="sso">
          <LoginSamlSsoForm absoluteRedirect={absoluteRedirect} />
        </Tab>
      </Tabs>

      {maybeOAuthError && (
        <FormError
          error={
            Array.isArray(maybeOAuthError)
              ? { message: maybeOAuthError[0] }
              : { message: maybeOAuthError }
          }
        />
      )}

      <Divider />

      <Flex flexWrap>
        <UnstyledLink
          href={`${TRY_BASE_URL}?redirect=${encodeURIComponent(
            relativeRedirect
          )}`}
        >
          <Button kind="minimal" size={SIZE.compact}>
            Create a free account
          </Button>
        </UnstyledLink>

        <Button kind="minimal" size={SIZE.compact}>
          Join an existing team
        </Button>
      </Flex>
    </AuthPageLayout>
  );
};
