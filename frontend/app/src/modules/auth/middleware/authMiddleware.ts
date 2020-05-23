import { OutgoingHttpHeaders } from 'http';

import { NextPageContext } from 'next';
import nextCookie from 'next-cookies';
import Router from 'next/router';
import SsoApi from 'api/sso';
import { UserDTO, DataResponse } from '@insight/types';
import { isServer } from 'shared/utils/next';

// eslint-disable-next-line @typescript-eslint/explicit-module-boundary-types
const authMiddleware = async (ctx: NextPageContext) => {
  const { pathname } = ctx;
  const { SessionId } = nextCookie(ctx);

  const redirectToLogin = (headers?: OutgoingHttpHeaders) => {
    const Location = `/login?dest=${encodeURIComponent(pathname)}`;
    if (isServer(ctx)) {
      ctx.res.writeHead(302, { Location, ...headers });
      ctx.res.end();
    } else {
      Router.push(Location);
    }
  };

  if (!SessionId) {
    return redirectToLogin();
  }

  if (!isServer(ctx)) {
    return undefined;
  }

  const response = await SsoApi.session(
    SessionId,
    process.env.AUTH_API_BASE_URL
  );
  if (response.status === 204) {
    const setCookie = response.headers.get('set-cookie');
    return redirectToLogin({ 'set-cookie': setCookie || undefined });
  }

  const dataResponse = (await response.json()) as DataResponse<UserDTO>;
  return dataResponse.data;
};

export default authMiddleware;
