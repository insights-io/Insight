import type { OutgoingHttpHeaders } from 'http';

import type { Span } from 'opentracing';
import type { GetServerSidePropsContext, GetServerSideProps } from 'next';
import type {
  UserDTO,
  DataResponse,
  SessionInfoDTO,
  OrganizationDTO,
} from '@rebrowse/types';
import {
  startSpan,
  startRequestSpan,
  prepareCrossServiceHeaders,
} from 'shared/utils/tracing';
import { LOGIN_PAGE, VERIFICATION_PAGE } from 'shared/constants/routes';
import { client } from 'sdk';
import nextCookie from 'next-cookies';

export type Authenticated = {
  organization: OrganizationDTO;
  user: UserDTO;
  SessionId: string;
};

export const authenticated = async (
  context: GetServerSidePropsContext,
  requestSpan: Span
): Promise<Authenticated | undefined> => {
  const { SessionId, ChallengeId } = nextCookie(context);
  const { url } = context.req;

  const span = startSpan('authMiddleware.authenticated', {
    childOf: requestSpan,
    tags: { SessionId, ChallengeId, url },
  });

  const redirect = (location: string, headers?: OutgoingHttpHeaders) => {
    let Location = location;
    if (url) {
      const [pathname, rest] = url.split('?');
      Location += `?redirect=${encodeURIComponent(pathname)}`;
      if (rest) {
        Location += `&${rest}`;
      }
      span.log({ message: `Redirecting to ${Location}` });
    }
    context.res.writeHead(302, { Location, ...headers });
    context.res.end();
    return undefined;
  };

  const redirectToLogin = (headers?: OutgoingHttpHeaders) => {
    return redirect(LOGIN_PAGE, headers);
  };

  const redirectToVerification = (headers?: OutgoingHttpHeaders) => {
    return redirect(VERIFICATION_PAGE, headers);
  };

  if (!SessionId) {
    if (ChallengeId) {
      span.log({ message: 'Missing SessionId: redirect to verification' });
      return redirectToVerification();
    }

    span.log({
      message: 'Missing SessionId and ChallengeId: redirect to login',
    });
    return redirectToLogin();
  }

  span.setTag('SessionId', SessionId);

  try {
    const responsePromise = client.auth.sso.sessions.retrieve(SessionId, {
      headers: prepareCrossServiceHeaders(span),
    });

    const response = await responsePromise;

    if (response.status === 204) {
      span.log({ message: 'Session expired' });
      const setCookie = response.headers.get('set-cookie');
      if (ChallengeId) {
        span.log({
          message:
            'Expired SessionId but ChallengeId present: redirect to verification',
        });
        return redirectToVerification();
      }
      return redirectToLogin({ 'set-cookie': setCookie || undefined });
    }

    const {
      data: { organization, user },
    } = await responsePromise.json<DataResponse<SessionInfoDTO>>();

    span.setTag('user.id', user.id);
    span.setTag('organization.id', organization.id);
    return { user, organization, SessionId };
  } finally {
    span.finish();
  }
};

export type AuthenticatedServerSideProps = Pick<
  Authenticated,
  'user' | 'organization'
>;

export const getAuthenticatedServerSideProps: GetServerSideProps<AuthenticatedServerSideProps> = async (
  context
) => {
  const requestSpan = startRequestSpan(context.req);
  try {
    const response = await authenticated(context, requestSpan);
    if (response) {
      return {
        props: { user: response.user, organization: response.organization },
      };
    }

    // This can never happen -- user is redirected
    return {
      props: {
        user: (null as unknown) as UserDTO,
        organization: (null as unknown) as OrganizationDTO,
      },
    };
  } finally {
    requestSpan.finish();
  }
};
