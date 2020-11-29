import { mapSsoSetup, mapUser } from '@rebrowse/sdk';
import type { UserDTO, User, SsoSetupDTO } from '@rebrowse/types';

export const REBROWSE_ADMIN_DTO: UserDTO = {
  id: '7c071176-d186-40ac-aaf8-ac9779ab047b',
  email: 'admin@rebrowse.dev',
  fullName: 'Admin Admin',
  organizationId: '000000',
  role: 'admin',
  createdAt: new Date().toUTCString(),
  updatedAt: new Date().toUTCString(),
  phoneNumber: { countryCode: '+386', digits: '51111222' },
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

export const TOTP_MFA_SETUP_QR_IMAGE =
  'iVBORw0KGgoAAAANSUhEUgAAAMgAAADICAIAAAAiOjnJAAAABmJLR0QA/wD/AP+gvaeTAAAFHklEQVR4nO3dwY7jNhBF0XQw///Lk0UALwSPYJl1KSU4ZzntttTjB6JMFcmf379//wXT/r77Bvh/EiwSgkVCsEgIFgnBIvHr7b/+/PzsufxhsuNw3ZWfnl/o3OCff37d879oxV2f4IsRi4RgkRAsEoJF4n3xfvCQonKwlr90G5eue36hSy8evI39n6ARi4RgkRAsEoJF4qPi/WCl8l2xUq4OVuvnuocHg9P0Gz5BIxYJwSIhWCQEi8Q3xXtnsCA9vNWlKe/B6fKDwVafruVmhBGLhGCRECwSgkXiWcX7to6UlSaTS7Png18p/luMWCQEi4RgkRAsEt8U790kb7cGdVDXVHPpuis2/NcZsUgIFgnBIiFYJD4q3rdNAT+kJXzlnQf3zxm8q/2T+EYsEoJFQrBICBaJn6f1Sp/oKtDB1a0Ht2/ZeBcjFgnBIiFYJASLxPvi/Zmzut3E9Iq7enu6xa4j312MWCQEi4RgkRAsEh8V7wddZ0jXZHJu23W37S9/+3WNWCQEi4RgkRAsEvNtM3fV8p27Hjzc1SA/8lZGLBKCRUKwSAgWiYFDmi5tf3ipIN22v8pgAf7MBavnv1swYpEQLBKCRUKwSHyz28y2GfDBdvJtb3X+zts6f84NPg75EyMWCcEiIVgkBIvENz3vB9t6wC/dxso7r8yer1zoXDfVXqyqNWKRECwSgkVCsEjs3ipycJJ35USnu7bT2fbQ4q4LvRixSAgWCcEiIVgkBmbeL3nIGar7F3C+ffHBtu6d7q1ejFgkBIuEYJEQLBLfLFjtSt3DTwf7uM9ffHDpd7d9Hdl/Suq/vntoYcQiIVgkBIuEYJH4qG3mroK062a5a4/4ldvYtjXNyGMJIxYJwSIhWCQEi8Q3Pe9dK/r5hbrej8G/aNuF7uopMvPOnQSLhGCRECwS79tmLlXNl6rIbSesdkthu870lUn8wS1xnLDKcwkWCcEiIVgk3hfv3UFLDzmP9FKv/eB3iG273Nx+OK0Ri4RgkRAsEoJF4ptDmg66mfcVg1P8d51CtWLlQ9HzznMJFgnBIiFYJPLdZrqe9+4kqcG1oNu2iBlsgR9ZiGDEIiFYJASLhGCR+Kh4XykMu99dOQe161u/ZPMJWX9S/IFGLBKCRUKwSAgWiW9m3i/ZttvM4JT3XQtHB3edvOs0qBcjFgnBIiFYJASLxDe7zazMYq9Ml1+6ya6WP3jI94/Bndz1vPNcgkVCsEgIFon3+7x3W5mf/+7KbVy60F1dNF1NPXhXI5+gEYuEYJEQLBKCReKj4n1byTlYrnaLTret113R9dhYsMqdBIuEYJEQLBLzPe8rvS4rtu1FczDYvbNt45ru4cGLEYuEYJEQLBKCReKbQ5pWXnxucI57sH/80k8v3dWgbee+fnhdIxYJwSIhWCQEi8RHC1Y7Kz3vgwtl7zr39fzFK2/VHQP74f+VEYuEYJEQLBKCRSLf5/1g23LWbn/5bkHAXYdSFS3/RiwSgkVCsEgIFolvet63tZNfum6x3+En1x28q5XnARva2N9eSNsMWwkWCcEiIVgk8kOaOtu2qNzWTj44iX+w8s7aZngQwSIhWCQEi8Szivdts+fbngcM1vLdgasFIxYJwSIhWCQEi8Q3xXu3Zcolg4emdg3yK7exUsvfdQrVixGLhGCRECwSgkXio+J927zttj1SVqb4t9XFd93GyLG3RiwSgkVCsEgIFon3J6zCIiMWCcEiIVgkBIuEYJEQLBKCReIfySK4shMx96wAAAAASUVORK5CYII=';

export const SSO_SAML_SETUP_DTO = {
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

export const SSO_SAML_SETUP = mapSsoSetup(SSO_SAML_SETUP_DTO as SsoSetupDTO);
