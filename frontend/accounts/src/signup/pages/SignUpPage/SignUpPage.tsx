import React from 'react';
import Head from 'next/head';
import Link from 'next/link';
import { H1, Paragraph1, Paragraph4 } from 'baseui/typography';
import { Block } from 'baseui/block';
import { SignUpForm } from 'signup/components/SignUpForm';
import { Flex } from '@rebrowse/elements';
import { sdk } from 'api';
import { seoTitle } from 'shared/utils/seo';
import { Layout } from 'shared/components/Layout';

const TITLE = 'Start your free trial now.';

export const SignUpPage = () => {
  return (
    <Layout>
      {([css, theme]) => (
        <>
          <Head>
            <title>{seoTitle(TITLE)}</title>
          </Head>

          <Block className={css({ textAlign: 'center' })}>
            <H1
              className={css({ fontSize: '32px' })}
              marginBottom={theme.sizing.scale400}
              marginTop={0}
            >
              {TITLE}
            </H1>

            <Paragraph1
              as="h2"
              color={theme.colors.primary400}
              marginTop={theme.sizing.scale400}
            >
              You&apos;re minutes away from insights.
            </Paragraph1>
          </Block>

          <Block marginTop={theme.sizing.scale1000}>
            <SignUpForm onSubmit={sdk.signup.create} />
          </Block>

          <Flex justifyContent="center">
            <Paragraph4 marginTop={theme.sizing.scale400}>
              Already have an account?{' '}
              <Link href="/">
                <a>Log in</a>
              </Link>
            </Paragraph4>
          </Flex>
        </>
      )}
    </Layout>
  );
};
