import React from 'react';
import Head from 'next/head';
import { useStyletron } from 'baseui';
import { H5, Paragraph3 } from 'baseui/typography';
import { Block } from 'baseui/block';
import { SignUpForm } from 'components/SignUpForm';
import { appBaseURL, helpBaseURL } from 'shared/config';
import { Topbar } from 'components/Topbar';
import { FlexColumn } from '@rebrowse/elements';
import { sdk } from 'api';

export const GetStarted = () => {
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

          <SignUpForm onSubmit={sdk.create} />
        </Block>
      </Block>
    </FlexColumn>
  );
};
