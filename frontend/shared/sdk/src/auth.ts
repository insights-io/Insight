import ky from 'ky-universal';
import { DataResponse, UserDTO } from '@insight/types';
import { RequestOptions } from 'types';

export const createAuthClient = (authApiBaseURL: string) => {
  const login = (
    email: string,
    password: string,
    { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
  ) => {
    const body = new URLSearchParams();
    body.set('email', email);
    body.set('password', password);
    return ky.post(`${baseURL}/v1/sso/login`, {
      body,
      credentials: 'include',
      ...rest,
    });
  };

  const session = (
    sessionId: string,
    { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
  ) => {
    return ky.get(`${baseURL}/v1/sso/session`, {
      searchParams: { id: sessionId },
      ...rest,
    });
  };

  const me = ({ baseURL = authApiBaseURL, ...rest }: RequestOptions = {}) => {
    return ky
      .get(`${baseURL}/v1/sso/me`, { credentials: 'include', ...rest })
      .json<DataResponse<UserDTO>>()
      .then((response) => response.data);
  };

  const logout = ({
    baseURL = authApiBaseURL,
    ...rest
  }: RequestOptions = {}) => {
    return ky.post(`${baseURL}/v1/sso/logout`, {
      credentials: 'include',
      ...rest,
    });
  };

  const logoutFromAllDevices = ({
    baseURL = authApiBaseURL,
    ...rest
  }: RequestOptions = {}) => {
    return ky.post(`${baseURL}/v1/sso/logout-from-all-devices`, {
      credentials: 'include',
      ...rest,
    });
  };

  return { sso: { login, session, me, logout, logoutFromAllDevices } };
};
