import React from 'react';
import type { GetServerSideProps } from 'next';
import { getQueryParam } from 'shared/utils/query';
import { SignInPage, Props } from 'signin/pages/SignInPage';
import { LOGIN_HINT_QUERY, REDIRECT_QUERY } from 'shared/constants/routes';
import { appBaseUrl } from 'shared/config';

export default function SignIn(props: Props) {
  return <SignInPage {...props} />;
}

export const getServerSideProps: GetServerSideProps = async (ctx) => {
  return {
    props: {
      email: getQueryParam(ctx.query, LOGIN_HINT_QUERY),
      redirect: getQueryParam(ctx.query, REDIRECT_QUERY) || appBaseUrl,
    },
  };
};
