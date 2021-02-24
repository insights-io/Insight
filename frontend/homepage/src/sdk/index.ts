import {
  createHttpClient,
  ssoSessionResource as createSsoSessionResource,
} from '@rebrowse/sdk';
import { AUTH_API_BASE_URL } from 'shared/constants';

export const client = createSsoSessionResource(
  createHttpClient(),
  process.env.AUTH_API_BASE_URL || AUTH_API_BASE_URL
);
