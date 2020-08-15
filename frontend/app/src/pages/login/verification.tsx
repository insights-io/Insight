import { OutgoingHttpHeaders } from 'http';

import React from 'react';
import { GetServerSideProps } from 'next';
import nextCookie from 'next-cookies';
import VerificationPage from 'modules/auth/pages/VerificationPage';
import { startRequestSpan, prepareCrossServiceHeaders } from 'modules/tracing';
import AuthApi from 'api/auth';
import { APIErrorDataResponse } from '@insight/types';

const Verification = () => {
  return <VerificationPage />;
};

export const getServerSideProps: GetServerSideProps = async (context) => {
  const requestSpan = startRequestSpan(context.req);
  try {
    const { VerificationId } = nextCookie(context);
    const { dest = '/' } = context.query;

    const redirectToLogin = (headers?: OutgoingHttpHeaders) => {
      const Location = `/login?dest=${encodeURIComponent(dest as string)}`;
      context.res.writeHead(302, { Location, ...headers });
      context.res.end();
      return { props: {} };
    };

    if (!VerificationId) {
      requestSpan.log({ message: 'Missing VerificationId' });
      return redirectToLogin();
    }

    try {
      await AuthApi.sso.verification(VerificationId, {
        baseURL: process.env.AUTH_API_BASE_URL,
        headers: prepareCrossServiceHeaders(requestSpan),
      });
    } catch (error) {
      const errorDTO: APIErrorDataResponse = await error.response.json();
      if (errorDTO.error.statusCode === 404) {
        requestSpan.log({ message: 'Expired verificaiton' });
        return redirectToLogin();
      }
      throw error;
    }

    return { props: {} };
  } finally {
    requestSpan.finish();
  }
};

export default Verification;
