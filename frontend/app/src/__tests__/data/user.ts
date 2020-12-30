import { mapSsoSetup, mapUser } from '@rebrowse/sdk';
import type { UserDTO, User, SsoSetupDTO, PhoneNumber } from '@rebrowse/types';

export const SLOVENIAN_PHONE_NUMBER: PhoneNumber = {
  countryCode: '+386',
  digits: '51111222',
};

export const REBROWSE_ADMIN_DTO: UserDTO = {
  id: '7c071176-d186-40ac-aaf8-ac9779ab047b',
  email: 'admin@rebrowse.dev',
  fullName: 'Admin Admin',
  organizationId: '000000',
  role: 'admin',
  createdAt: new Date().toUTCString(),
  updatedAt: new Date().toUTCString(),
  phoneNumber: SLOVENIAN_PHONE_NUMBER,
  phoneNumberVerified: true,
};

export const REBROWSE_ADMIN_NO_PHONE_NUMBER: User = mapUser({
  ...REBROWSE_ADMIN_DTO,
  id: '7c071176-d186-40ac-aaf8-ac9779ab047c',
  phoneNumber: undefined,
  phoneNumberVerified: false,
});

export const NAMELESS_ADMIN_DTO: UserDTO = {
  ...REBROWSE_ADMIN_DTO,
  id: '7c071176-d186-40ac-aaf8-ac9779ab047d',
  fullName: undefined,
};

export const NAMELESS_ADMIN: User = mapUser(NAMELESS_ADMIN_DTO);
export const REBROWSE_ADMIN: User = mapUser(REBROWSE_ADMIN_DTO);

export const SSO_SAML_SETUP_DTO: SsoSetupDTO = {
  saml: {
    method: 'okta',
    metadataEndpoint:
      'https://example.okta.com/app/exkw843tlucjMJ0kL4x6/sso/saml/metadata',
  },
  createdAt: new Date().toISOString(),
  domain: 'snuderls.eu',
  organizationId: '000001',
  method: 'saml',
};

export const SSO_SAML_SETUP = mapSsoSetup(SSO_SAML_SETUP_DTO);
