import React from 'react';
import Head from 'next/head';
import Topbar from 'components/Topbar';
import config from 'shared/config';
import { Block } from 'baseui/block';
import { useStyletron } from 'baseui';
import { H6 } from 'baseui/typography';

const SignupConfirm = () => {
  const [_css, theme] = useStyletron();
  return (
    <Block display="flex" flexDirection="column" height="100%">
      <Head>
        <title>Insight | Confirm your email address</title>
      </Head>
      <Topbar appBaseURL={config.appBaseURL} helpBaseURL={config.helpBaseURL} />
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

export default SignupConfirm;
