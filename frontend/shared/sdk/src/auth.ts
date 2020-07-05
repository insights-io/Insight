import ky from 'ky-universal';
import { DataResponse, UserDTO } from '@insight/types';
import { InsightRequestOptions } from 'types';

export const createInsightAuthClient = (authApiBaseURL: string) => {
  const login = (
    email: string,
    password: string,
    { baseURL = authApiBaseURL, ...rest }: InsightRequestOptions = {}
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
    { baseURL = authApiBaseURL, ...rest }: InsightRequestOptions = {}
  ) => {
    return ky.get(`${baseURL}/v1/sso/session`, {
      searchParams: { id: sessionId },
      ...rest,
    });
  };

  const me = ({
    baseURL = authApiBaseURL,
    ...rest
  }: InsightRequestOptions = {}) => {
    return ky
      .get(`${baseURL}/v1/sso/me`, { credentials: 'include', ...rest })
      .json<DataResponse<UserDTO>>()
      .then((response) => response.data);
  };

  const logout = ({
    baseURL = authApiBaseURL,
    ...rest
  }: InsightRequestOptions = {}) => {
    return ky.post(`${baseURL}/v1/sso/logout`, {
      credentials: 'include',
      ...rest,
    });
  };

  const logoutFromAllDevices = ({
    baseURL = authApiBaseURL,
    ...rest
  }: InsightRequestOptions = {}) => {
    return ky.post(`${baseURL}/v1/sso/logout-from-all-devices`, {
      credentials: 'include',
      ...rest,
    });
  };

  return { login, session, me, logout, logoutFromAllDevices };
};
