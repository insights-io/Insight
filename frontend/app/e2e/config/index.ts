export default {
  appBaseURL: process.env.APP_BASE_URL || 'http://localhost:3000',
  tryBaseURL:
    process.env.NEXT_PUBLIC_ACCOUNTS_BASE_URL || 'http://localhost:3002',
  rebrowseUserEmail: process.env.REBROWSE_USER_EMAIL || 'admin@rebrowse.dev',
  rebrowseUserPassword:
    process.env.REBROWSE_USER_PASSWORD || 'superDuperPassword123',
};
