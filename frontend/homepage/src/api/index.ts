import { ssoSessionApi as createSsoSessionApi } from '@insight/sdk';
import { AUTH_API_BASE_URL } from 'shared/constants';

export default createSsoSessionApi(AUTH_API_BASE_URL);
