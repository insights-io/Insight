import { OutgoingHttpHeaders } from 'http';

import { Span } from 'opentracing';
import { GetServerSidePropsContext, GetServerSideProps } from 'next';
import nextCookie from 'next-cookies';
import { AuthApi } from 'api';
import { DataResponse, UserDTO } from '@insight/types';
import {
  getTracer,
  startRequestSpan,
  prepareCrossServiceHeaders,
} from 'modules/tracing';

export const authenticated = async (
  context: GetServerSidePropsContext,
  requestSpan: Span
): Promise<UserDTO | unknown> => {
  const { SessionId } = nextCookie(context);
  const pathname = context.req.url;
  const span = getTracer().startSpan('authMiddleware.authenticated', {
    childOf: requestSpan,
  });

  const redirectToLogin = (headers?: OutgoingHttpHeaders) => {
    let Location = '/login';
    if (pathname) {
      Location += `?dest=${encodeURIComponent(pathname)}`;
    }
    context.res.writeHead(302, { Location, ...headers });
    context.res.end();
    return {};
  };

  if (!SessionId) {
    span.log({ message: 'Missing session id' });
    span.finish();
    return redirectToLogin();
  }
  span.setTag('SessionId', SessionId);

  try {
    const response = await AuthApi.sso.session(SessionId, {
      baseURL: process.env.AUTH_API_BASE_URL,
      headers: prepareCrossServiceHeaders(span),
    });

    if (response.status === 204) {
      span.log({ message: 'Session expired' });
      const setCookie = response.headers.get('set-cookie');
      return redirectToLogin({ 'set-cookie': setCookie || undefined });
    }
    const dataResponse = (await response.json()) as DataResponse<UserDTO>;
    span.setTag('user.id', dataResponse.data.id);
    return dataResponse.data;
  } finally {
    span.finish();
  }
};

export type AuthenticatedServerSideProps = {
  user: UserDTO;
};

export const getAuthenticatedServerSideProps: GetServerSideProps<AuthenticatedServerSideProps> = async (
  context
) => {
  const requestSpan = startRequestSpan(context.req);
  try {
    const user = (await authenticated(context, requestSpan)) as UserDTO;
    return { props: { user } };
  } finally {
    requestSpan.finish();
  }
};
