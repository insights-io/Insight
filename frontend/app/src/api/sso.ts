import ky from 'ky-universal';

import { authApiBaseURL } from './base';

const SsoApi = {
  login: (email: string, password: string, baseURL = authApiBaseURL) => {
    const body = new URLSearchParams();
    body.set('email', email);
    body.set('password', password);
    return ky
      .post(`${baseURL}/v1/sso/login`, { body, credentials: 'include' })
      .json();
  },
  session: (sessionId: string, baseURL = authApiBaseURL) => {
    return ky.get(`${baseURL}/v1/sso/session`, {
      searchParams: { id: sessionId },
    });
  },
  me: (baseURL = authApiBaseURL) => {
    return ky.get(`${baseURL}/v1/sso/me`, { credentials: 'include' });
  },
  logout: (baseURL = authApiBaseURL) => {
    return ky.post(`${baseURL}/v1/sso/logout`, { credentials: 'include' });
  },
  logoutFromAllDevices: (baseURL = authApiBaseURL) => {
    return ky.post(`${baseURL}/v1/sso/logout-from-all-devices`, {
      credentials: 'include',
    });
  },
};

export default SsoApi;
