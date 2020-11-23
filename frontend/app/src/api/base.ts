export const authApiBaseURL = (process.env.NEXT_PUBLIC_AUTH_API_BASE_URL ||
  process.env.NEXT_PUBLIC_REBROWSE_API_BASE_URL) as string;

export const sessionApiBaseURL = (process.env
  .NEXT_PUBLIC_SESSION_API_BASE_URL ||
  process.env.NEXT_PUBLIC_REBROWSE_API_BASE_URL) as string;

export const billingApiBaseURL = (process.env
  .NEXT_PUBLIC_BILLING_API_BASE_URL ||
  process.env.NEXT_PUBLIC_REBROWSE_API_BASE_URL) as string;
