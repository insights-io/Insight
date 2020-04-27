import React from 'react';
import { NextPageContext } from 'next';
import Router from 'next/router';
import SignupApi, { SignupRequestDTO } from 'api/signup';
import SignupComplete from 'modules/auth/components/SignupComplete';

type Props = {
  signupRequest: SignupRequestDTO;
};

const SignupCompletePage = ({ signupRequest }: Props) => {
  return <SignupComplete {...signupRequest} />;
};

SignupCompletePage.getInitialProps = async (ctx: NextPageContext) => {
  const { email, orgId, token } = ctx.query;

  const invalidSignupRedirect = () => {
    const Location = '/invalid-sign-up';
    if (ctx.res) {
      ctx.res.writeHead(302, { Location });
      ctx.res.end();
    } else {
      Router.push(Location);
    }
  };

  if (!email || !orgId || !token) {
    invalidSignupRedirect();
  }

  const response = await SignupApi.verify({
    email: email as string,
    token: token as string,
    org: orgId as string,
  });

  if (response.data === false) {
    invalidSignupRedirect();
  }

  return { signupRequest: { email, token, org: orgId } };
};

export default SignupCompletePage;
