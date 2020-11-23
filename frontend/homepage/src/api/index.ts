import { ssoSessionResource } from '@rebrowse/sdk';
import { AUTH_API_BASE_URL } from 'shared/constants';

export const ssoResource = ssoSessionResource(AUTH_API_BASE_URL);
