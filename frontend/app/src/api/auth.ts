import { createAuthClient } from '@rebrowse/sdk';

import { authApiBaseURL } from './base';

export const AuthApi = createAuthClient(authApiBaseURL);
