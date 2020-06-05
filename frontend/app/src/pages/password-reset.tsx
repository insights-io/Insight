import React from 'react';
import PasswordApi from 'api/password';
import { GetServerSideProps } from 'next';
import InvalidPasswordResetRequest from 'modules/auth/components/InvalidPasswordResetRequest';
import PasswordReset from 'modules/auth/components/PasswordReset';

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

const PasswordResetPage = (props: Props) => {
  if (!passwordResetRequest(props)) {
    return <InvalidPasswordResetRequest />;
  }

  const { token } = props;
  return <PasswordReset token={token} />;
};

export const getServerSideProps: GetServerSideProps<Props> = async (ctx) => {
  const token = ctx.query.token as string | undefined;

  if (!token) {
    return { props: { exists: false } };
  }

  const response = await PasswordApi.resetExists(
    token,
    process.env.AUTH_API_BASE_URL
  );
  if (response.data === false) {
    return { props: { exists: false } };
  }
  return { props: { exists: true, token } };
};

export default PasswordResetPage;
