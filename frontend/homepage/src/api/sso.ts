import ky from 'ky-universal';

import { baseURL } from './base';

const SsoApi = {
  session: (sessionId: string) => {
    return ky.get(`${baseURL}/v1/sso/session`, {
      searchParams: { id: sessionId },
    });
  },
};

export default SsoApi;
