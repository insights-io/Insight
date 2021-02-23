import React from 'react';
import type { GetServerSideProps } from 'next';
import { client } from 'sdk';
import {
  SignInPwdChallengePage,
  Props,
} from 'signin/pages/SignInPwdChallengePage';
import {
  PWD_CHALLENGE_SESSION_ID,
  SIGNIN_ROUTE,
} from 'shared/constants/routes';

export default function SignInPwdChallenge(props: Props) {
  return <SignInPwdChallengePage {...props} />;
}

export const getServerSideProps: GetServerSideProps<Props> = async (ctx) => {
  const { [PWD_CHALLENGE_SESSION_ID]: challengeId } = ctx.req.cookies || {};
  try {
    const { data } = await client.accounts.retrievePwdChallenge(challengeId);
    return { props: data };
  } catch (error) {
    const response = error.response as Response;
    if (response.status === 404) {
      // TODO: clear AuthorizationPwdChallengeSessionId cookie
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
