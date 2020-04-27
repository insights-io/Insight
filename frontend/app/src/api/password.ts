import ky from 'ky-universal';

import { baseURL, DataResponse } from './base';

export type PasswordResetRequestBase = {
  email: string;
  token: string;
  org: string;
};

export type PasswordResetRequestDTO = PasswordResetRequestBase & {
  password: string;
};

const PasswordApi = {
  forgot: (email: string) => {
    return ky
      .post(`${baseURL}/v1/password/forgot`, { json: { email } })
      .json<DataResponse<boolean>>();
  },
  reset: (json: PasswordResetRequestDTO) => {
    return ky
      .post(`${baseURL}/v1/password/reset`, { json, credentials: 'include' })
      .json<DataResponse<boolean>>();
  },
  resetExists: (searchParams: PasswordResetRequestBase) => {
    return ky
      .get(`${baseURL}/v1/password/reset/exists`, { searchParams })
      .json<DataResponse<boolean>>();
  },
};

export default PasswordApi;
