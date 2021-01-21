import React from 'react';
import Head from 'next/head';
import { AccountsLayout } from 'shared/components/AccountsLayout';
import { seoTitle } from 'shared/utils/seo';
import { SpacedBetween } from '@rebrowse/elements';
import { FaEnvelope } from 'react-icons/fa';

const TITLE = 'Check your inbox!';

export const CheckYourInboxPage = () => {
  return (
    <AccountsLayout>
      {() => (
        <>
          <Head>
            <title>{seoTitle(TITLE)}</title>
          </Head>
          <SpacedBetween>
            <AccountsLayout.Header>{TITLE}</AccountsLayout.Header>
            <FaEnvelope size={64} />
          </SpacedBetween>

          <AccountsLayout.SubHeader>
            If your email address is associated with an Rebrowse account, you
            will be receiving a password reset request shortly.
          </AccountsLayout.SubHeader>
        </>
      )}
    </AccountsLayout>
  );
};
