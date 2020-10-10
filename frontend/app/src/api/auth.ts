import { createAuthClient } from '@insight/sdk';

import { authApiBaseURL } from './base';

export const AuthApi = createAuthClient(authApiBaseURL);
