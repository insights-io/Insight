import React from 'react';
import type { GetServerSideProps } from 'next';
import {
  MFA_CHALLENGE_SESSION_ID,
  PWD_CHALLENGE_SESSION_ID,
  SIGNIN_PWD_CHALLENGE_ROUTE,
  SIGNIN_ROUTE,
} from 'shared/constants/routes';
import { client } from 'sdk';
import { SignInMfaChallengePage } from 'signin/pages/SignInMfaChallengePage';
import { SignInMfaChallengeEnforcedPage } from 'signin/pages/SignInMfaChallengeEnforcedPage';
import type { MfaMethod } from '@rebrowse/types';

type Props = { methods: MfaMethod[] };

export default function SignInMfaChallenge({ methods }: Props) {
  if (methods.length === 0) {
    return (
      <SignInMfaChallengeEnforcedPage
        // TODO: properly implement
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        user={{ phoneNumber: undefined } as any}
      />
    );
  }

  return <SignInMfaChallengePage methods={methods} />;
}

export const getServerSideProps: GetServerSideProps<Props> = async (ctx) => {
  const {
    [MFA_CHALLENGE_SESSION_ID]: mfaChallengeId,
    [PWD_CHALLENGE_SESSION_ID]: pwdChallengeId,
  } = ctx.req.cookies || {};

  if (!mfaChallengeId) {
    if (pwdChallengeId) {
      return {
        redirect: {
          destination: SIGNIN_PWD_CHALLENGE_ROUTE,
          statusCode: 302,
        },
      };
    }
    return {
      redirect: {
        destination: SIGNIN_ROUTE,
        statusCode: 302,
      },
    };
  }

  try {
    const {
      data: { methods },
    } = await client.accounts.retrieveMfaChallenge(mfaChallengeId);

    return { props: { methods } };
  } catch (error) {
    const response = error.response as Response;
    if (response.status === 404) {
      // TODO: clear AuthorizationMfaChallengeSessionId cookie
      return {
        redirect: {
          destination: SIGNIN_ROUTE,
          statusCode: 302,
        },
      };
    }
    throw error;
  }
};
