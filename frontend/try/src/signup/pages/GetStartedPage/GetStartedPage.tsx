import React from 'react';
import Head from 'next/head';
import { useStyletron } from 'baseui';
import { H1, H2 } from 'baseui/typography';
import { Block } from 'baseui/block';
import { SignUpForm } from 'signup/components/SignUpForm';
import { Topbar } from 'signup/components/Topbar';
import { FlexColumn } from '@rebrowse/elements';
import { appBaseURL, helpBaseURL } from 'shared/config';
import { sdk } from 'api';

export const GetStartedPage = () => {
  const [_css, theme] = useStyletron();

  return (
    <FlexColumn height="100%">
      <Head>
        <title>Rebrowse | Sign up</title>
      </Head>
      <Topbar appBaseURL={appBaseURL} helpBaseURL={helpBaseURL} />
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
            <H1
              marginBottom={theme.sizing.scale400}
              $style={{ fontSize: '40px' }}
            >
              Start your free trial now.
            </H1>

            <H2
              marginTop={theme.sizing.scale400}
              color={theme.colors.primary400}
              $style={{ fontSize: '22px' }}
            >
              You&apos;re minutes away from insights.
            </H2>
          </Block>

          <SignUpForm onSubmit={sdk.signup.create} />
        </Block>
      </Block>
    </FlexColumn>
  );
};
