import React from 'react';
import { AuthApi } from 'api/auth';
import type { GetServerSideProps } from 'next';
import { PasswordResetInvalidPage } from 'auth/pages/PasswordResetInvalidPage';
import { PasswordResetPage } from 'auth/pages/PasswordResetPage';
import {
  startRequestSpan,
  prepareCrossServiceHeaders,
} from 'shared/utils/tracing';

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

const PasswordReset = (props: Props) => {
  if (!passwordResetRequest(props)) {
    return <PasswordResetInvalidPage />;
  }

  const { token } = props;
  return <PasswordResetPage token={token} />;
};

export const getServerSideProps: GetServerSideProps<Props> = async (
  context
) => {
  const requestSpan = startRequestSpan(context.req);
  try {
    const token = context.query.token as string | undefined;
    if (!token) {
      return { props: { exists: false } };
    }

    const { data: exists } = await AuthApi.password.resetExists(token, {
      baseURL: process.env.AUTH_API_BASE_URL,
      headers: prepareCrossServiceHeaders(requestSpan),
    });
    return { props: { exists, token } };
  } finally {
    requestSpan.finish();
  }
};

export default PasswordReset;
