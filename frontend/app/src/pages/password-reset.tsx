import React from 'react';
import { PasswordResetRequest } from '@insight/types';
import PasswordApi from 'api/password';
import { GetServerSideProps } from 'next';
import InvalidPasswordResetRequest from 'modules/auth/components/InvalidPasswordResetRequest';
import PasswordReset from 'modules/auth/components/PasswordReset';

type NonExistingPasswordResetRequest = {
  exists: false;
};

type ExistingPasswordResetRequest = PasswordResetRequest & {
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

  const { email, token, org } = props;
  return <PasswordReset email={email} token={token} org={org} />;
};

export const getServerSideProps: GetServerSideProps<Props> = async (ctx) => {
  const { email: emailQuery, orgId: orgQuery, token: tokenQuery } = ctx.query;

  if (!emailQuery || !orgQuery || !tokenQuery) {
    return { props: { exists: false } };
  }

  const email = emailQuery as string;
  const token = tokenQuery as string;
  const org = orgQuery as string;

  const response = await PasswordApi.resetExists({ email, token, org });
  if (response.data === false) {
    return { props: { exists: false } };
  }
  return { props: { exists: true, email, token, org } };
};

export default PasswordResetPage;
