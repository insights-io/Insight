/* eslint-disable no-param-reassign */
/* eslint-disable @typescript-eslint/no-explicit-any */
import React from 'react';
import { UserDTO } from '@insight/types';
import authMiddleware from 'modules/auth/middleware/authMiddleware';
import { NextPageContext, NextComponentType } from 'next';

type WithAuthProps = {
  user?: UserDTO;
};

type ExcludeAuthProps<P> = Pick<P, Exclude<keyof P, keyof WithAuthProps>>;

const authenticated = <P extends WithAuthProps, C = NextPageContext>(
  WrappedPage: NextComponentType<NextPageContext, any, C>
): React.ComponentType<ExcludeAuthProps<P>> => {
  const AuthenticatedPage = (props: any) => {
    return <WrappedPage {...props} />;
  };

  if (process.env.NODE_ENV !== 'production') {
    const name = WrappedPage.displayName || WrappedPage.name || 'Unknown';
    AuthenticatedPage.displayName = `authenticated(${name})`;
  }

  AuthenticatedPage.getInitialProps = async (context: NextPageContext) => {
    const user = await authMiddleware(context);

    const wrappedPageProps =
      WrappedPage.getInitialProps &&
      (await WrappedPage.getInitialProps(context));

    return { ...wrappedPageProps, user };
  };

  return AuthenticatedPage;
};

export default authenticated;
