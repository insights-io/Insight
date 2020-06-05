import ky from 'ky-universal';
import { DataResponse } from '@insight/types';

import { authApiBaseURL } from './base';

const PasswordApi = {
  forgot: (email: string, baseURL = authApiBaseURL) => {
    return ky
      .post(`${baseURL}/v1/password_forgot`, { json: { email } })
      .json<DataResponse<boolean>>();
  },
  reset: (token: string, password: string, baseURL = authApiBaseURL) => {
    return ky
      .post(`${baseURL}/v1/password_reset/${token}`, {
        json: { password },
        credentials: 'include',
      })
      .json<DataResponse<boolean>>();
  },
  resetExists: (token: string, baseURL = authApiBaseURL) => {
    return ky
      .get(`${baseURL}/v1/password_reset/${token}/exists`)
      .json<DataResponse<boolean>>();
  },
};

export default PasswordApi;
