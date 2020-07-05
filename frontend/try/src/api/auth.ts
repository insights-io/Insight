import { createAuthClient } from '@insight/sdk';

import { authApiBaseURL } from './base';

const AuthApi = createAuthClient(authApiBaseURL);

export default AuthApi;
