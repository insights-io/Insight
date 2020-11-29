/* eslint-disable react/destructuring-assignment */
import type { OutgoingHttpHeaders } from 'http';

import React from 'react';
import type { GetServerSideProps } from 'next';
import nextCookie from 'next-cookies';
import { VerificationPage } from 'modules/auth/pages/VerificationPage';
import { startRequestSpan, prepareCrossServiceHeaders } from 'modules/tracing';
import { AuthApi } from 'api/auth';
import type { APIErrorDataResponse, MfaMethod, UserDTO } from '@rebrowse/types';
import { LOGIN_PAGE } from 'shared/constants/routes';
import { SetupMultiFactorAuthenticationPage } from 'modules/auth/pages/SetupMultiFactorAuthenticationPage';

type Props =
  | { methods: MfaMethod[]; user?: undefined }
  | { methods?: undefined; user: UserDTO };

const Verification = (props: Props) => {
  if (props.methods) {
    return <VerificationPage methods={props.methods} />;
  }

  return <SetupMultiFactorAuthenticationPage user={props.user} />;
};

export const getServerSideProps: GetServerSideProps<Props> = async (
  context
) => {
  const requestSpan = startRequestSpan(context.req);
  try {
    const { ChallengeId } = nextCookie(context);
    const relativeRedirect = (context.query.redirect || '/') as string;
    const redirectToLogin = (headers?: OutgoingHttpHeaders) => {
      const Location = `${LOGIN_PAGE}?redirect=${encodeURIComponent(
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
      const methods = await AuthApi.mfa.challenge.get(ChallengeId, {
        baseURL: process.env.AUTH_API_BASE_URL,
        headers: prepareCrossServiceHeaders(requestSpan),
      });

      if (methods.length > 0) {
        return { props: { methods } };
      }

      const user = await AuthApi.mfa.challenge.retrieveUser(ChallengeId, {
        baseURL: process.env.AUTH_API_BASE_URL,
        headers: prepareCrossServiceHeaders(requestSpan),
      });

      return { props: { user } };
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
