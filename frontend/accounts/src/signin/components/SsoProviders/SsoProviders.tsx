import React from 'react';
import { Button, UnstyledLink } from '@rebrowse/elements';
import { Block } from 'baseui/block';
import { createOAuth2IntegrationHrefBuilder } from 'signin/utils';
import { FaGithub, FaMicrosoft } from 'react-icons/fa';
import type { Theme } from 'baseui/theme';

type Props = {
  redirect: string;
  theme: Theme;
};

export const SsoProviders = ({ redirect, theme }: Props) => {
  const oauth2IntegrationHrefBuilder = createOAuth2IntegrationHrefBuilder({
    redirect,
  });

  return (
    <Block>
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
            // @ts-expect-error missing taypings
            tabIndex={-1}
          >
            Sign in with Microsoft
          </Button>
        </UnstyledLink>
      </Block>
    </Block>
  );
};
