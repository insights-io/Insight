import type { SsoMethod } from '@insight/types';

export type SsoMethodSelectValue = { label: string; id: SsoMethod };

export type SsoSetupFormData = {
  method: SsoMethodSelectValue;
  configurationEndpoint?: string;
};

const SAML_METHOD = { label: 'SAML', id: 'saml' } as const;

export const SSO_OPTIONS: SsoMethodSelectValue[] = [
  SAML_METHOD,
  { label: 'Google', id: 'google' },
  { label: 'Microsoft', id: 'microsoft' },
  { label: 'Github', id: 'github' },
];

export const findSsoSelectValue = (method: SsoMethod) => {
  return SSO_OPTIONS.find((o) => o.id === method) as SsoMethodSelectValue;
};
