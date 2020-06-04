import { SignUpFormDTO } from '@insight/types';
import ky from 'ky-universal';

import { authApiBaseURL } from './base';

export const SignupApi = {
  signup: (json: SignUpFormDTO) => {
    return ky.post(`${authApiBaseURL}/v1/signup`, { json }).json();
  },
};
