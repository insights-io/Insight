import { getEnvOverrides, getApiProxy } from './config';

describe('config', () => {
  describe('getEnvOverrides', () => {
    it('Should correctly override variables', () => {
      process.env.PROXY = 'staging';
      expect(getEnvOverrides()).toEqual({
        AUTH_API_BASE_URL: 'https://auth-api.dev.snuderls.eu',
        BILLING_API_BASE_URL: 'https://billing-api.dev.snuderls.eu',
        SESSION_API_BASE_URL: 'https://session-api.dev.snuderls.eu',
        NEXT_PUBLIC_AUTH_API_BASE_URL: '/api/auth',
        NEXT_PUBLIC_BILLING_API_BASE_URL: '/api/billing',
        NEXT_PUBLIC_SESSION_API_BASE_URL: '/api/session',
      });
    });
  });

  describe('getApiProxy', () => {
    it('Should correctly proxy api', () => {
      process.env.PROXY = 'staging';
      expect(getApiProxy('auth')).toEqual('https://auth-api.dev.snuderls.eu');
      expect(getApiProxy('billing')).toEqual(
        'https://billing-api.dev.snuderls.eu'
      );
      expect(getApiProxy('session')).toEqual(
        'https://session-api.dev.snuderls.eu'
      );
    });
  });
});
