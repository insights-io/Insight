import React from 'react';
import { useStyletron } from 'baseui';
import { Block } from 'baseui/block';
import { Button, SHAPE } from 'baseui/button';
import { H6 } from 'baseui/typography';
import { GetServerSideProps } from 'next';
import nextCookie from 'next-cookies';
import SsoApi from 'api/sso';
import SpacedBetween from 'shared/components/flex/SpacedBetween';
import UnstyledA from 'shared/components/UnstyledA';

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

  const response = await SsoApi.session(SessionId);
  return { props: { loggedIn: response.status === 200 } };
};

export default Home;
