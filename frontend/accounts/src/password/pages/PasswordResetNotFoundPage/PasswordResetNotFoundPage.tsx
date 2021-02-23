import React from 'react';
import Head from 'next/head';
import { AccountsLayout } from 'shared/components/AccountsLayout';
import { seoTitle } from 'shared/utils/seo';
import { Button, SpacedBetween, UnstyledLink } from '@rebrowse/elements';
import { FaTimes } from 'react-icons/fa';
import { SIGNIN_ROUTE } from 'shared/constants/routes';
import Link from 'next/link';

const TITLE = 'Password reset not found';

export const PasswordResetNotFoundPage = () => {
  return (
    <AccountsLayout>
      {({ theme }) => (
        <>
          <Head>
            <title>{seoTitle(TITLE)}</title>
          </Head>
          <SpacedBetween>
            <AccountsLayout.Header>{TITLE}</AccountsLayout.Header>
            <FaTimes size={64} />
          </SpacedBetween>

          <AccountsLayout.SubHeader marginBottom={theme.sizing.scale800}>
            It looks like this password reset request is invalid or has already
            been accepted.
          </AccountsLayout.SubHeader>

          <Link href={SIGNIN_ROUTE}>
            <UnstyledLink href={SIGNIN_ROUTE}>
              <Button
                $style={{ width: '100%' }}
                // @ts-expect-error missing typings
                tabIndex={-1}
              >
                Back to sign in
              </Button>
            </UnstyledLink>
          </Link>
        </>
      )}
    </AccountsLayout>
  );
};
