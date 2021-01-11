import React from 'react';
import { NextPageContext } from 'next';
import { isServer } from 'shared/utils/next';
import Router from 'next/router';
import { ErrorPage } from 'shared/pages/ErrorPage';
import type { ErrorProps } from 'next/error';

type Props = ErrorProps;

const Error = ({ statusCode }: Props) => {
  return <ErrorPage statusCode={statusCode} />;
};

Error.getInitialProps = (context: NextPageContext) => {
  let statusCode: number | undefined;

  if (isServer(context) && context.res.writeHead) {
    statusCode = context.res.statusCode;
    if (statusCode === 404) {
      context.res.writeHead(302, { Location: '/' });
      context.res.end();
      return { statusCode };
    }
  } else {
    statusCode = context.err?.statusCode;
    if (statusCode === 404) {
      Router.push('/');
      return { statusCode };
    }
  }

  return { statusCode };
};

export default Error;
