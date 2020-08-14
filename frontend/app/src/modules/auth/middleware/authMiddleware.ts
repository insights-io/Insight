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

export type Authenticated = {
  user: UserDTO;
  SessionId: string;
};

export const authenticated = async (
  context: GetServerSidePropsContext,
  requestSpan: Span
): Promise<Authenticated | undefined> => {
  const { SessionId, VerificationId } = nextCookie(context);
  const pathname = context.req.url;

  const span = getTracer().startSpan('authMiddleware.authenticated', {
    childOf: requestSpan,
    tags: { SessionId, VerificationId, pathname },
  });

  const redirect = (location: string, headers?: OutgoingHttpHeaders) => {
    let Location = location;
    if (pathname) {
      Location += `?dest=${encodeURIComponent(pathname)}`;
    }
    context.res.writeHead(302, { Location, ...headers });
    context.res.end();
    return undefined;
  };

  const redirectToLogin = (headers?: OutgoingHttpHeaders) => {
    return redirect('/login', headers);
  };

  const redirectToVerification = (headers?: OutgoingHttpHeaders) => {
    return redirect('/login/verification', headers);
  };

  if (!SessionId) {
    if (VerificationId) {
      span.log({ message: 'Missing SessionId: redirect to verification' });
      return redirectToVerification();
    }

    span.log({
      message: 'Missing SessionId and VerificationId: redirect to login',
    });
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
      if (VerificationId) {
        span.log({
          message:
            'Expired SessionId but verificationId presend: redirect to verification',
        });
        return redirectToVerification();
      }
      return redirectToLogin({ 'set-cookie': setCookie || undefined });
    }
    const dataResponse = (await response.json()) as DataResponse<UserDTO>;
    span.setTag('user.id', dataResponse.data.id);
    return { user: dataResponse.data, SessionId };
  } finally {
    span.finish();
  }
};

export type AuthenticatedServerSideProps = Pick<Authenticated, 'user'>;

export const getAuthenticatedServerSideProps: GetServerSideProps<AuthenticatedServerSideProps> = async (
  context
) => {
  const requestSpan = startRequestSpan(context.req);
  try {
    const { user } = (await authenticated(
      context,
      requestSpan
    )) as Authenticated;
    return { props: { user } };
  } finally {
    requestSpan.finish();
  }
};
