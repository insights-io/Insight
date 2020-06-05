import React from 'react';
import Head from 'next/head';
import { useStyletron } from 'baseui';
import { H5, Paragraph3 } from 'baseui/typography';
import { Block } from 'baseui/block';
import SignUpForm from 'components/SignUpForm';
import config from 'shared/config';
import Topbar from 'components/Topbar';
import { SignupApi } from 'api/signup';

const GetStarted = () => {
  const [_css, theme] = useStyletron();

  return (
    <Block display="flex" flexDirection="column" height="100%">
      <Head>
        <title>Insight | Sign up</title>
      </Head>
      <Topbar appBaseURL={config.appBaseURL} helpBaseURL={config.helpBaseURL} />
      <Block height="100%" padding={theme.sizing.scale600}>
        <Block
          width="100%"
          maxWidth="720px"
          marginLeft="auto"
          marginRight="auto"
        >
          <Block
            marginBottom={theme.sizing.scale700}
            $style={{ textAlign: 'center' }}
          >
            <H5
              marginBottom={theme.sizing.scale400}
              $style={{ fontWeight: 700 }}
            >
              Start your free trial now.
            </H5>

            <Paragraph3
              marginTop={theme.sizing.scale400}
              color={theme.colors.primary400}
            >
              You&apos;re minutes away from insights.
            </Paragraph3>
          </Block>

          <SignUpForm onSubmit={SignupApi.signup} />
        </Block>
      </Block>
    </Block>
  );
};

export default GetStarted;
