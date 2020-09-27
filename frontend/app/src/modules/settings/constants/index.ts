import { SearchOption } from 'modules/settings/types';
import {
  ACCOUNT_SETTINGS_SECURITY_PAGE,
  ORGANIZATION_SETTINGS_AUTH_PAGE,
  ORGANIZATION_SETTINGS_SECURITY_AND_PRIVACY_PAGE,
} from 'shared/constants/routes';

export const SETTINGS_SEARCH_OPTIONS: SearchOption[] = [
  {
    label: 'Auth',
    link: ORGANIZATION_SETTINGS_AUTH_PAGE,
    description: 'Configure single sign-on',
  },
  {
    label: 'Require Two-Factor Authentication',
    link: ORGANIZATION_SETTINGS_SECURITY_AND_PRIVACY_PAGE,
    description:
      'Require and enforce two-factor authentication for all members',
  },
  {
    label: 'Change Password',
    description: 'Change current password',
    link: ACCOUNT_SETTINGS_SECURITY_PAGE,
  },
];
