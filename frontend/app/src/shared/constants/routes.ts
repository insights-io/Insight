import type { PathPart } from 'modules/settings/types';

export const INDEX_PAGE = '/';

/* Auth */
export const LOGIN_PAGE = '/login';

/* Settings */
export const SETTINGS_PATH_PART: PathPart = {
  segment: 'settings',
  text: 'Settings',
};

export const SETTINGS_PAGE = `/${SETTINGS_PATH_PART.segment}`;

/* Organization settings */
export const ORGANIZATION_SETTINGS_PAGE_PART: PathPart = {
  segment: 'organization',
  text: 'Organization',
};
export const ORGANIZATION_SETTINGS_PAGE = `${SETTINGS_PAGE}/${ORGANIZATION_SETTINGS_PAGE_PART.segment}`;
export const ORGANIZATION_SETTINGS_GENERAL_PAGE_PART: PathPart = {
  segment: 'general',
  text: 'General',
};
export const ORGANIZATION_SETTINGS_GENERAL_PAGE = `${ORGANIZATION_SETTINGS_PAGE}/${ORGANIZATION_SETTINGS_GENERAL_PAGE_PART.segment}`;

export const ORGANIZATION_SETTINGS_SECURITY_AND_PRIVACY_PAGE_PART: PathPart = {
  segment: 'security-and-privacy',
  text: 'Security & Privacy',
};
export const ORGANIZATION_SETTINGS_SECURITY_AND_PRIVACY_PAGE = `${ORGANIZATION_SETTINGS_PAGE}/${ORGANIZATION_SETTINGS_SECURITY_AND_PRIVACY_PAGE_PART.segment}`;
export const ORGANIZATION_SETTINGS_AUTH_PAGE_PART: PathPart = {
  segment: 'auth',
  text: 'Auth',
};
export const ORGANIZATION_SETTINGS_AUTH_PAGE = `${ORGANIZATION_SETTINGS_PAGE}/${ORGANIZATION_SETTINGS_AUTH_PAGE_PART.segment}`;
export const ORGANIZATION_SETTINGS_MEMBERS_PAGE_PART: PathPart = {
  segment: 'members',
  text: 'Members',
};
export const ORGANIZATION_SETTINGS_MEMBERS_PAGE = `${ORGANIZATION_SETTINGS_PAGE}/${ORGANIZATION_SETTINGS_MEMBERS_PAGE_PART.segment}`;

export const ORGANIZATION_SETTINGS_BILLING_SUBSCRIPTION_PAGE_PART: PathPart = {
  segment: 'subscription',
  text: 'Subscription',
};
export const ORGANIZATION_SETTINGS_BILLING_SUBSCRIPTION_PAGE = `${ORGANIZATION_SETTINGS_PAGE}/${ORGANIZATION_SETTINGS_BILLING_SUBSCRIPTION_PAGE_PART.segment}`;
export const ORGANIZATION_SETTINGS_BILLING_USAGE_AND_PAYMENTS_PAGE_PART: PathPart = {
  segment: 'usage-and-payments',
  text: 'Usage & Payments',
};
export const ORGANIZATION_SETTINGS_BILLING_USAGE_AND_PAYMENTS_PAGE = `${ORGANIZATION_SETTINGS_PAGE}/${ORGANIZATION_SETTINGS_BILLING_USAGE_AND_PAYMENTS_PAGE_PART.segment}`;

/* Account settings */
export const ACCOUNT_SETTINGS_PATH_PART: PathPart = {
  segment: 'account',
  text: 'Account',
};
export const ACCOUNT_SETTINGS_PAGE = `${SETTINGS_PAGE}/${ACCOUNT_SETTINGS_PATH_PART.segment}`;

export const ACCOUNT_SETTINGS_AUTH_TOKENS_PATH_PART: PathPart = {
  segment: 'auth-tokens',
  text: 'Auth Tokens',
};
export const ACCOUNT_SETTINGS_AUTH_TOKENS_PAGE = `${ACCOUNT_SETTINGS_PAGE}/${ACCOUNT_SETTINGS_AUTH_TOKENS_PATH_PART.segment}`;

export const ACCOUNT_SETTINGS_DETAILS_PAGE_PART: PathPart = {
  segment: 'details',
  text: 'Details',
};
export const ACCOUNT_SETTINGS_DETAILS_PAGE = `${ACCOUNT_SETTINGS_PAGE}/${ACCOUNT_SETTINGS_DETAILS_PAGE_PART.segment}`;

export const ACCOUNT_SETTINGS_SECURITY_PAGE_PART: PathPart = {
  segment: 'security',
  text: 'Security',
};
export const ACCOUNT_SETTINGS_SECURITY_PAGE = `${ACCOUNT_SETTINGS_PAGE}/${ACCOUNT_SETTINGS_SECURITY_PAGE_PART.segment}`;

/* Sessions */
export const SESSIONS_PAGE = '/sessions';
