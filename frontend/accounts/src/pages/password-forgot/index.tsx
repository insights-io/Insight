import React from 'react';
import { PasswordForgotPage, Props } from 'password/pages/PasswordForgotPage';
import type { GetServerSideProps } from 'next';
import { LOGIN_HINT_QUERY, REDIRECT_QUERY } from 'shared/constants/routes';
import { getQueryParam } from 'shared/utils/query';
import { appBaseUrl } from 'shared/config';

export default function PasswordForgot({ email, redirect }: Props) {
  return <PasswordForgotPage email={email} redirect={redirect} />;
}

export const getServerSideProps: GetServerSideProps = async (ctx) => {
  return {
    props: {
      email: getQueryParam(ctx.query, LOGIN_HINT_QUERY),
      redirect: getQueryParam(ctx.query, REDIRECT_QUERY) || appBaseUrl,
    },
  };
};
