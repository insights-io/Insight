import { getEnvOverrides, getApiProxy } from './config';

describe('config', () => {
  describe('getEnvOverrides', () => {
    it('Should correctly override variables', () => {
      process.env.PROXY = 'staging';
      expect(getEnvOverrides()).toEqual({
        AUTH_API_BASE_URL: 'https://auth-api.snuderls.dev',
        BILLING_API_BASE_URL: 'https://billing-api.snuderls.dev',
        SESSION_API_BASE_URL: 'https://session-api.snuderls.dev',
        NEXT_PUBLIC_AUTH_API_BASE_URL: '/api/auth',
        NEXT_PUBLIC_BILLING_API_BASE_URL: '/api/billing',
        NEXT_PUBLIC_SESSION_API_BASE_URL: '/api/session',
      });
    });
  });

  describe('getApiProxy', () => {
    it('Should correctly proxy api', () => {
      process.env.PROXY = 'staging';
      expect(getApiProxy('auth')).toEqual('https://auth-api.snuderls.dev');
      expect(getApiProxy('billing')).toEqual(
        'https://billing-api.snuderls.dev'
      );
      expect(getApiProxy('session')).toEqual(
        'https://session-api.snuderls.dev'
      );
    });
  });
});
