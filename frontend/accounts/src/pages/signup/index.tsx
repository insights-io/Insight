import React from 'react';
import type { GetServerSideProps } from 'next';
import { SignUpPage } from 'signup/pages/SignUpPage';
import { getQueryParam } from 'shared/utils/query';
import { LOGIN_HINT_QUERY, REDIRECT_QUERY } from 'shared/constants/routes';
import { appBaseUrl } from 'shared/config';

type Props = {
  email: string | null;
  redirect: string;
};

export default function Signup({ email, redirect }: Props) {
  return <SignUpPage email={email ?? undefined} redirect={redirect} />;
}

export const getServerSideProps: GetServerSideProps<Props> = async (ctx) => {
  return {
    props: {
      email: getQueryParam(ctx.query, LOGIN_HINT_QUERY),
      redirect: getQueryParam(ctx.query, REDIRECT_QUERY) || appBaseUrl,
    },
  };
};
