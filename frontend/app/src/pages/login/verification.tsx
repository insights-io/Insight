import { OutgoingHttpHeaders } from 'http';

import React from 'react';
import { GetServerSideProps } from 'next';
import nextCookie from 'next-cookies';
import VerificationPage from 'modules/auth/pages/VerificationPage';
import { startRequestSpan, prepareCrossServiceHeaders } from 'modules/tracing';
import AuthApi from 'api/auth';
import { APIErrorDataResponse, TfaMethod } from '@insight/types';

type Props = {
  methods: TfaMethod[];
};

const Verification = ({ methods }: Props) => {
  return <VerificationPage methods={methods} />;
};

export const getServerSideProps: GetServerSideProps<Props> = async (
  context
) => {
  const requestSpan = startRequestSpan(context.req);
  try {
    const { ChallengeId } = nextCookie(context);
    const relativeRedirect = (context.query.redirect || '/') as string;
    const redirectToLogin = (headers?: OutgoingHttpHeaders) => {
      const Location = `/login?redirect=${encodeURIComponent(
        relativeRedirect
      )}`;
      context.res.writeHead(302, { Location, ...headers });
      context.res.end();
      return { props: {} as Props };
    };

    if (!ChallengeId) {
      requestSpan.log({ message: 'Missing ChallengeId' });
      return redirectToLogin();
    }

    try {
      const methods = await AuthApi.tfa.getChallenge(ChallengeId, {
        baseURL: process.env.AUTH_API_BASE_URL,
        headers: prepareCrossServiceHeaders(requestSpan),
      });

      return { props: { methods } };
    } catch (error) {
      const errorDTO: APIErrorDataResponse = await error.response.json();
      if (errorDTO.error.statusCode === 404) {
        requestSpan.log({ message: 'Expired verificaiton' });
        return redirectToLogin();
      }
      throw error;
    }
  } finally {
    requestSpan.finish();
  }
};

export default Verification;
