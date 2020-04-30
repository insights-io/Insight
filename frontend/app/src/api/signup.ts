import ky from 'ky-universal';
import { DataResponse, SignupRequest, Signup } from '@insight/types';

import { baseURL } from './base';

const SignupApi = {
  verify: (json: SignupRequest) => {
    const url = `${baseURL}/v1/signup/verify`;
    return ky.post(url, { json }).json<DataResponse<boolean>>();
  },
  complete: (json: Signup) => {
    return ky
      .post(`${baseURL}/v1/signup/complete`, { json })
      .json<DataResponse<boolean>>();
  },
};

export default SignupApi;
