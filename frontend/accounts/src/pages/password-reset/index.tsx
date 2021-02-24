import React from 'react';
import type { GetServerSideProps } from 'next';
import { client } from 'sdk';
import { PasswordResetNotFoundPage } from 'password/pages/PasswordResetNotFoundPage';
import { PasswordResetPage } from 'password/pages/PasswordResetPage';

type NonExistingPasswordResetRequest = {
  exists: false;
};

type ExistingPasswordResetRequest = {
  token: string;
  exists: true;
};

type Props = ExistingPasswordResetRequest | NonExistingPasswordResetRequest;

const passwordResetRequest = (
  props: Props
): props is ExistingPasswordResetRequest => {
  return props.exists === true;
};

export default function PasswordReset(props: Props) {
  if (!passwordResetRequest(props)) {
    return <PasswordResetNotFoundPage />;
  }

  const { token } = props;
  return <PasswordResetPage token={token} />;
}

export const getServerSideProps: GetServerSideProps<Props> = async (
  context
) => {
  const token = context.query.token as string | undefined;
  if (!token) {
    return { props: { exists: false } };
  }

  const { data: exists } = await client.password.resetExists(token);
  return { props: { exists, token } };
};
