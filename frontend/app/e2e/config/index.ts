export default {
  appBaseURL: process.env.APP_BASE_URL || 'http://localhost:3000',
  tryBaseURL: process.env.NEXT_PUBLIC_TRY_BASE_URL || 'http://localhost:3002',
  insightUserEmail: process.env.INSIGHT_USER_EMAIL || 'admin@insight.io',
  insightUserPassword:
    process.env.INSIGHT_USER_PASSWORD || 'superDuperPassword123',
};
