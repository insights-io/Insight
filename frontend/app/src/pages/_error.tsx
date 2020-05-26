import React from 'react';
import { NextPageContext } from 'next';
import { isServer } from 'shared/utils/next';
import Router from 'next/router';

type Props = {
  statusCode: number;
};

const ErrorPage = ({ statusCode }: Props) => {
  return (
    <p>
      {statusCode
        ? `An error ${statusCode} occurred on server`
        : 'An error occurred on client'}
    </p>
  );
};

ErrorPage.getInitialProps = (context: NextPageContext) => {
  let statusCode: number | undefined;

  if (isServer(context) && context.res.writeHead) {
    statusCode = context.res.statusCode;
    if (statusCode === 404) {
      context.res.writeHead(302, { Location: '/' });
      context.res.end();
      return {};
    }
  } else {
    statusCode = context.err?.statusCode;
    if (statusCode === 404) {
      Router.push('/');
      return {};
    }
  }

  return { statusCode };
};

export default ErrorPage;
