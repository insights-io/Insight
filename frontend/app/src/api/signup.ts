import ky from 'ky-universal';
import { DataResponse, SignupRequest, Signup } from '@insight/types';

import { authApiBaseURL } from './base';

const SignupApi = {
  verify: (json: SignupRequest, baseURL = authApiBaseURL) => {
    const url = `${baseURL}/v1/signup/verify`;
    return ky.post(url, { json }).json<DataResponse<boolean>>();
  },
  complete: (json: Signup, baseURL = authApiBaseURL) => {
    return ky
      .post(`${baseURL}/v1/signup/complete`, { json })
      .json<DataResponse<boolean>>();
  },
};

export default SignupApi;
