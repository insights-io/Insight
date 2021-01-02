import {
  createHttpClient,
  ssoSessionResource as createSsoSessionResource,
} from '@rebrowse/sdk';
import { AUTH_API_BASE_URL } from 'shared/constants';

const client = createHttpClient();

export const sdk =
  typeof document === 'undefined'
    ? createSsoSessionResource(client, process.env.AUTH_API_BASE_URL as string)
    : createSsoSessionResource(client, AUTH_API_BASE_URL);
