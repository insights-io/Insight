import { OutgoingHttpHeaders } from 'http';

import { GetServerSidePropsContext, GetServerSideProps } from 'next';
import nextCookie from 'next-cookies';
import { AuthApi } from 'api';
import { DataResponse, UserDTO } from '@insight/types';

const authMiddleware = async (
  context: GetServerSidePropsContext
): Promise<UserDTO | unknown> => {
  const { SessionId } = nextCookie(context);
  const pathname = context.req.url;

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
    return redirectToLogin();
  }

  const response = await AuthApi.sso.session(
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

export type AuthMiddlewareProps = {
  user: UserDTO;
};

export const getServerSideAuthProps: GetServerSideProps<AuthMiddlewareProps> = async (
  context
) => {
  const user = (await authMiddleware(context)) as UserDTO;
  return { props: { user } };
};

export default authMiddleware;
