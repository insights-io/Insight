import ky from 'ky-universal';
import { DataResponse } from '@insight/types';

import { authApiBaseURL } from './base';

const SignupApi = {
  verify: (token: string, baseURL = authApiBaseURL) => {
    return ky
      .get(`${baseURL}/v1/signup/${token}/valid`)
      .json<DataResponse<boolean>>();
  },
  complete: (token: string, baseURL = authApiBaseURL) => {
    return ky.post(`${baseURL}/v1/signup/${token}/complete`);
  },
};

export default SignupApi;
