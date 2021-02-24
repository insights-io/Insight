import React from 'react';
import Head from 'next/head';
import { useStyletron } from 'baseui';
import { Block } from 'baseui/block';
import { Button, SHAPE } from 'baseui/button';
import { H6 } from 'baseui/typography';
import UnstyledA from 'shared/components/UnstyledA';
import { APP_BASE_URL, ACCOUNTS_BASE_URL } from 'shared/constants';

type Props = {
  loggedIn: boolean;
};

export const HomePage = ({ loggedIn }: Props) => {
  const [_css, theme] = useStyletron();
  const href = loggedIn ? APP_BASE_URL : ACCOUNTS_BASE_URL;

  return (
    <>
      <Head>
        <title>Find insights into your frontend applications.</title>
      </Head>
      <Block padding={theme.sizing.scale300}>
        <Block display="flex" justifyContent="space-between">
          <H6 margin={0}>Rebrowse</H6>
          <UnstyledA href={href}>
            <Button shape={SHAPE.pill} size="compact">
              {loggedIn ? 'Go to app' : 'Get started'}
            </Button>
          </UnstyledA>
        </Block>
      </Block>
    </>
  );
};
