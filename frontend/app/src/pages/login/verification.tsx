import { OutgoingHttpHeaders } from 'http';

import React from 'react';
import { GetServerSideProps } from 'next';
import nextCookie from 'next-cookies';
import VerificationPage from 'modules/auth/pages/VerificationPage';
import { startRequestSpan } from 'modules/tracing';

const Verification = () => {
  return <VerificationPage />;
};

export const getServerSideProps: GetServerSideProps = async (context) => {
  const requestSpan = startRequestSpan(context.req);
  const { VerificationId } = nextCookie(context);
  const { dest = '/' } = context.query;

  const redirectToLogin = (headers?: OutgoingHttpHeaders) => {
    const Location = `/login?dest=${encodeURIComponent(dest as string)}`;
    context.res.writeHead(302, { Location, ...headers });
    context.res.end();
    requestSpan.finish();
    return { props: {} };
  };

  if (!VerificationId) {
    requestSpan.log({ message: 'Missing VerificationId' });
    return redirectToLogin();
  }

  requestSpan.finish();
  return { props: {} };
};

export default Verification;
