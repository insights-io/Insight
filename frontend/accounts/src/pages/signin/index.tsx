import React from 'react';
import type { GetServerSideProps } from 'next';
import { getQueryParam } from 'shared/utils/query';
import { SignInPage, Props } from 'signin/pages/SignInPage';
import {
  PWD_CHALLENGE_SESSION_ID,
  LOGIN_HINT_QUERY,
  REDIRECT_QUERY,
  SIGNIN_PWD_CHALLENGE_ROUTE,
} from 'shared/constants/routes';
import { appBaseUrl } from 'shared/config';
import { client } from 'sdk';

export default function SignIn(props: Props) {
  return <SignInPage {...props} />;
}

export const getServerSideProps: GetServerSideProps = async (ctx) => {
  const { [PWD_CHALLENGE_SESSION_ID]: challengeId } = ctx.req.cookies || {};
  const email = getQueryParam(ctx.query, LOGIN_HINT_QUERY);

  if (challengeId && email) {
    const challenge = await client.accounts.retrievePwdChallenge(challengeId);

    if (challenge.data.email === email) {
      return {
        redirect: {
          destination: SIGNIN_PWD_CHALLENGE_ROUTE,
          statusCode: 302,
        },
      };
    }
  }

  return {
    props: {
      email,
      redirect: getQueryParam(ctx.query, REDIRECT_QUERY) || appBaseUrl,
    },
  };
};
