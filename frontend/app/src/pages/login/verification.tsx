/* eslint-disable react/destructuring-assignment */
import React from 'react';
import type { GetServerSideProps } from 'next';
import { VerificationPage } from 'auth/pages/VerificationPage';
import {
  startRequestSpan,
  prepareCrossServiceHeaders,
} from 'shared/utils/tracing';
import type { MfaMethod, UserDTO } from '@rebrowse/types';
import { LOGIN_PAGE } from 'shared/constants/routes';
import { SetupMultiFactorAuthenticationPage } from 'auth/pages/SetupMultiFactorAuthenticationPage';
import { client } from 'sdk';
import nextCookie from 'next-cookies';

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

    if (!ChallengeId) {
      requestSpan.log({ message: 'Missing ChallengeId' });
      return {
        redirect: {
          destination: `${LOGIN_PAGE}?redirect=${encodeURIComponent(
            relativeRedirect
          )}`,
          statusCode: 302,
        },
      };
    }

    const headers = prepareCrossServiceHeaders(requestSpan);
    try {
      const methods = await client.auth.mfa.challenge
        .retrieve(ChallengeId, { headers })
        .then((httpResponse) => httpResponse.data);

      if (methods.length > 0) {
        return { props: { methods } };
      }

      const user = await client.auth.mfa.challenge
        .retrieveCurrentUser(ChallengeId, { headers })
        .then((httpResponse) => httpResponse.data);

      return { props: { user } };
    } catch (error) {
      const response = error.response as Response;
      if (response.status === 404) {
        requestSpan.log({ message: 'Expired verificaiton' });
        // TODO: clear challengeId cookie?
        return {
          redirect: {
            destination: `${LOGIN_PAGE}?redirect=${encodeURIComponent(
              relativeRedirect
            )}`,
            statusCode: 302,
          },
        };
      }
      throw error;
    }
  } finally {
    requestSpan.finish();
  }
};

export default Verification;
