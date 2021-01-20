import React from 'react';
import Head from 'next/head';
import { H6 } from 'baseui/typography';
import { seoTitle } from 'shared/utils/seo';
import { Layout } from 'shared/components/Layout';

export const SignUpConfirmPage = () => {
  return (
    <Layout>
      {([css]) => (
        <>
          <Head>
            <title>{seoTitle('Confirm your email address')}</title>
          </Head>
          <H6 className={css({ textAlign: 'center' })}>
            We have sent an email with a confirmation link to your email
            address.
          </H6>
        </>
      )}
    </Layout>
  );
};
