import { getEnvOverrides, getApiProxy } from './config';

describe('config', () => {
  describe('getEnvOverrides', () => {
    it('Should correctly override variables', () => {
      process.env.PROXY = 'staging';
      expect(getEnvOverrides()).toEqual({
        AUTH_API_BASE_URL: 'https://api.rebrowse.dev',
        BILLING_API_BASE_URL: 'https://api.rebrowse.dev',
        SESSION_API_BASE_URL: 'https://api.rebrowse.dev',
        NEXT_PUBLIC_AUTH_API_BASE_URL: '/api/auth',
        NEXT_PUBLIC_BILLING_API_BASE_URL: '/api/billing',
        NEXT_PUBLIC_SESSION_API_BASE_URL: '/api/session',
      });
    });
  });

  describe('getApiProxy', () => {
    it('Should correctly proxy api', () => {
      process.env.PROXY = 'staging';
      expect(getApiProxy('auth')).toEqual('api.rebrowse.dev');
      expect(getApiProxy('billing')).toEqual('api.rebrowse.dev');
      expect(getApiProxy('session')).toEqual('api.rebrowse.dev');
    });
  });
});
