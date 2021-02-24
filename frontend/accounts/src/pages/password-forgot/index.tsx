import React from 'react';
import { PasswordForgotPage, Props } from 'password/pages/PasswordForgotPage';
import type { GetServerSideProps } from 'next';
import { LOGIN_HINT_QUERY } from 'shared/constants/routes';
import { getQueryParam } from 'shared/utils/query';

export default function PasswordForgot({ email }: Props) {
  return <PasswordForgotPage email={email} />;
}

export const getServerSideProps: GetServerSideProps = async (ctx) => {
  return {
    props: {
      email: getQueryParam(ctx.query, LOGIN_HINT_QUERY),
    },
  };
};
