import ky from 'ky-universal';
import {
  DataResponse,
  PasswordReset,
  PasswordResetRequest,
} from '@insight/types';

import { baseURL } from './base';

const PasswordApi = {
  forgot: (email: string) => {
    return ky
      .post(`${baseURL}/v1/password/forgot`, { json: { email } })
      .json<DataResponse<boolean>>();
  },
  reset: (json: PasswordReset) => {
    return ky
      .post(`${baseURL}/v1/password/reset`, { json, credentials: 'include' })
      .json<DataResponse<boolean>>();
  },
  resetExists: (searchParams: PasswordResetRequest) => {
    return ky
      .get(`${baseURL}/v1/password/reset/exists`, { searchParams })
      .json<DataResponse<boolean>>();
  },
};

export default PasswordApi;
