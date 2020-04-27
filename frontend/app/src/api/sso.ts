import ky from 'ky-universal';

import { baseURL } from './base';

const SsoApi = {
  login: (email: string, password: string) => {
    const body = new URLSearchParams();
    body.set('email', email);
    body.set('password', password);
    return ky
      .post(`${baseURL}/v1/sso/login`, { body, credentials: 'include' })
      .json();
  },
  session: (sessionId: string) => {
    return ky.get(`${baseURL}/v1/sso/session`, {
      searchParams: { id: sessionId },
    });
  },
  me: () => {
    return ky.get(`${baseURL}/v1/sso/me`, { credentials: 'include' });
  },
  logout: () => {
    return ky.post(`${baseURL}/v1/sso/logout`, { credentials: 'include' });
  },
};

export default SsoApi;
