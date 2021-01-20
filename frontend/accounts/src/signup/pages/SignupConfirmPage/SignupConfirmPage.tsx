import React from 'react';
import Head from 'next/head';
import { appBaseURL, helpBaseURL } from 'shared/config';
import { Topbar } from 'signup/components/Topbar';
import { Block } from 'baseui/block';
import { useStyletron } from 'baseui';
import { H6 } from 'baseui/typography';

export const SignupConfirmPage = () => {
  const [_css, theme] = useStyletron();
  return (
    <Block display="flex" flexDirection="column" height="100%">
      <Head>
        <title>Rebrowse | Confirm your email address</title>
      </Head>
      <Topbar appBaseURL={appBaseURL} helpBaseURL={helpBaseURL} />
      <Block
        height="100%"
        padding={theme.sizing.scale600}
        display="flex"
        flexDirection="column"
        justifyContent="center"
      >
        <Block
          width="100%"
          maxWidth="720px"
          marginLeft="auto"
          marginRight="auto"
          $style={{ textAlign: 'center' }}
        >
          <H6>
            We have sent an email with a confirmation link to your email
            address.
          </H6>
        </Block>
      </Block>
    </Block>
  );
};
