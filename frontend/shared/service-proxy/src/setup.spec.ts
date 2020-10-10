import { sandbox } from '@insight/testing';

import { setupEnv } from './setup';

describe('setupEnv', () => {
  it('Should not proxy anything when empty config', () => {
    const readConfig = sandbox.stub().returns({ parsed: {} });

    expect(setupEnv({ readConfig })).toEqual({});

    sandbox.assert.calledWithExactly(readConfig, { path: '.env.test' });
  });

  it('Should proxy when matches pattern', () => {
    process.env.NEXT_PUBLIC_AUTH_API_BASE_URL = 'https://auth-api.snuderls.dev';

    const readConfig = sandbox.stub().returns({
      parsed: {
        NEXT_PUBLIC_AUTH_API_BASE_URL: 'http://localhost:8080',
      },
    });

    expect(setupEnv({ readConfig })).toEqual({
      AUTH_API_BASE_URL: 'https://auth-api.snuderls.dev',
      NEXT_PUBLIC_AUTH_API_BASE_URL: '/api/auth',
      NEXT_PUBLIC_AUTH_API_PROXIED_BASE_URL: 'https://auth-api.snuderls.dev',
    });

    sandbox.assert.calledWithExactly(readConfig, { path: '.env.test' });
  });
});
