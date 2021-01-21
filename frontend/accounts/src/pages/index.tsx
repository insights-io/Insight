import React from 'react';
import type { GetServerSideProps } from 'next';
import { getQueryParam } from 'shared/utils/query';
import { SignInPage, Props } from 'signin/pages/SignInPage';
import { LOGIN_HINT_QUERY } from 'shared/constants/routes';

export default function SignIn({ email }: Props) {
  return <SignInPage email={email} />;
}

export const getServerSideProps: GetServerSideProps = async (ctx) => {
  return {
    props: {
      email: getQueryParam(ctx.query, LOGIN_HINT_QUERY),
    },
  };
};
