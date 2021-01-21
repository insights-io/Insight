import React from 'react';
import Head from 'next/head';
import Link from 'next/link';
import { H1, Paragraph1, Paragraph3 } from 'baseui/typography';
import { Block } from 'baseui/block';
import { SignUpForm } from 'signup/components/SignUpForm';
import { Flex } from '@rebrowse/elements';
import { sdk } from 'api';
import { seoTitle } from 'shared/utils/seo';
import { AccountsLayout } from 'shared/components/AccountsLayout';
import { StyledLink } from 'baseui/link';
import { SIGNIN_ROUTE } from 'shared/constants/routes';

const TITLE = 'Start your free trial now.';

export const SignUpPage = () => {
  return (
    <AccountsLayout>
      {({ css, theme }) => (
        <>
          <Head>
            <title>{seoTitle(TITLE)}</title>
          </Head>

          <Block className={css({ textAlign: 'center' })}>
            <H1
              marginBottom={theme.sizing.scale400}
              marginTop={0}
              $style={{
                fontWeight: 700,
                fontSize: theme.typography.font950.fontSize,
              }}
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
            <Paragraph3
              margin={theme.sizing.scale400}
              marginBottom={theme.sizing.scale600}
            >
              Already have an account?{' '}
              <Link href={SIGNIN_ROUTE}>
                <StyledLink href={SIGNIN_ROUTE}>Log in</StyledLink>
              </Link>
            </Paragraph3>
          </Flex>
        </>
      )}
    </AccountsLayout>
  );
};
