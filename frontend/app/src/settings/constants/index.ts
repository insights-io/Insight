import { SearchOption } from 'settings/types';
import {
  ACCOUNT_SETTINGS_SECURITY_PAGE,
  ORGANIZATION_SETTINGS_AUTH_PAGE,
  ORGANIZATION_SETTINGS_GENERAL_PAGE,
  ORGANIZATION_SETTINGS_SECURITY_PAGE,
} from 'shared/constants/routes';

export const SETTINGS_SEARCH_OPTIONS: SearchOption[] = [
  {
    label: 'Auth',
    link: ORGANIZATION_SETTINGS_AUTH_PAGE,
    description: 'Configure single sign-on',
  },
  {
    label: 'Require Multi-Factor Authentication',
    link: ORGANIZATION_SETTINGS_SECURITY_PAGE,
    description:
      'Require and enforce multi-factor authentication for all members',
  },
  {
    label: 'Set password policy',
    link: ORGANIZATION_SETTINGS_SECURITY_PAGE,
    description:
      'Password policy is a set of rules that define complexity requirements for your organization members',
  },
  {
    label: 'Change Password',
    description: 'Change current password',
    link: ACCOUNT_SETTINGS_SECURITY_PAGE,
  },
  {
    label: 'Configure avatar',
    description: 'Configure avatar for you organization',
    link: ORGANIZATION_SETTINGS_GENERAL_PAGE,
  },
];
