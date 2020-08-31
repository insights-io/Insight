import React from 'react';
import { useStyletron } from 'baseui';
import { Block } from 'baseui/block';
import { Button, SHAPE } from 'baseui/button';
import { H6 } from 'baseui/typography';
import { GetServerSideProps } from 'next';
import nextCookie from 'next-cookies';
import { createAuthClient } from '@insight/sdk';
import SpacedBetween from 'shared/components/flex/SpacedBetween';
import UnstyledA from 'shared/components/UnstyledA';

const AuthApi = createAuthClient(
  process.env.AUTH_API_BASE_URL || 'http://localhost:8080'
);

type Props = {
  loggedIn: boolean;
};

const Home = ({ loggedIn }: Props) => {
  const [_css, theme] = useStyletron();
  const href = loggedIn ? process.env.TRY_BASE_URL : process.env.APP_BASE_URL;

  return (
    <>
      <Block padding={theme.sizing.scale300}>
        <SpacedBetween>
          <H6 margin={0}>Insight</H6>
          <UnstyledA href={href}>
            <Button shape={SHAPE.pill} size="compact">
              {loggedIn ? 'Go to app' : 'Sign up'}
            </Button>
          </UnstyledA>
        </SpacedBetween>
      </Block>
    </>
  );
};

export const getServerSideProps: GetServerSideProps<Props> = async (ctx) => {
  const { SessionId } = nextCookie(ctx);
  if (!SessionId) {
    return { props: { loggedIn: false } };
  }

  const response = await AuthApi.sso.session(SessionId);
  return { props: { loggedIn: response.status === 200 } };
};

export default Home;
