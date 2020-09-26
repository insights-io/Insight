export const INDEX_PAGE = '/';

/* Auth */
export const LOGIN_PAGE = '/login';

/* Settings */
const SETTINGS_PAGE_COMMON_REFIX = `/settings`;

/* User settings */
export const USER_SETTINGS_PAGE = `${SETTINGS_PAGE_COMMON_REFIX}/user`;

/* Organization settings */
export const ORGANIZATION_SETTINGS_PAGE = `${SETTINGS_PAGE_COMMON_REFIX}/organization`;
export const ORGANIZATION_GENERAL_SETTINGS_PAGE = `${ORGANIZATION_SETTINGS_PAGE}/general`;
export const ORGANIZATION_SECURITY_SETTINGS_PAGE = `${ORGANIZATION_SETTINGS_PAGE}/security`;
export const ORGANIZATION_BILLING_SETTINGS_PAGE = `${ORGANIZATION_SETTINGS_PAGE}/billing`;

/* API keys settings */
export const API_KEYS_SETTINGS_PAGE = `${SETTINGS_PAGE_COMMON_REFIX}/api-keys`;

/* Sessions */
export const SESSIONS_PAGE = '/sessions';
