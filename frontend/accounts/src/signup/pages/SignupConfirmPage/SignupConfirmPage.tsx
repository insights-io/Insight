import React from 'react';
import Head from 'next/head';
import { seoTitle } from 'shared/utils/seo';
import { AccountsLayout } from 'shared/components/AccountsLayout';
import { FaEnvelope } from 'react-icons/fa';
import { SpacedBetween, VerticalAligned } from '@rebrowse/elements';

const TITLE = 'Confirm your email address!';

export const SignUpConfirmPage = () => {
  return (
    <AccountsLayout>
      {({ theme }) => (
        <>
          <Head>
            <title>{seoTitle(TITLE)}</title>
          </Head>
          <SpacedBetween marginBottom={theme.sizing.scale1000}>
            <AccountsLayout.Header margin={0}>{TITLE}</AccountsLayout.Header>
            <VerticalAligned>
              <FaEnvelope size={64} />
            </VerticalAligned>
          </SpacedBetween>

          <AccountsLayout.SubHeader>
            We have sent an email with a confirmation link to your email
            address.
          </AccountsLayout.SubHeader>
        </>
      )}
    </AccountsLayout>
  );
};
