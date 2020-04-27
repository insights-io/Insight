import ky from 'ky-universal';

import { PasswordResetRequestBase } from './password';
import { baseURL, DataResponse } from './base';

export type SignupRequestDTO = PasswordResetRequestBase;

export type CompleteSignupRequestDTO = SignupRequestDTO & {
  password: string;
};

const SignupApi = {
  verify: (json: SignupRequestDTO) => {
    const url = `${baseURL}/v1/signup/verify`;
    return ky.post(url, { json }).json<DataResponse<boolean>>();
  },
  complete: (json: CompleteSignupRequestDTO) => {
    return ky
      .post(`${baseURL}/v1/signup/complete`, { json })
      .json<DataResponse<boolean>>();
  },
};

export default SignupApi;
