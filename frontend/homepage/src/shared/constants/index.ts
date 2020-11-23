export const APP_BASE_URL = process.env.NEXT_PUBLIC_APP_BASE_URL as string;

export const TRY_BASE_URL = process.env.NEXT_PUBLIC_TRY_BASE_URL as string;

export const AUTH_API_BASE_URL = (process.env.NEXT_PUBLIC_AUTH_API_BASE_URL ||
  process.env.NEXT_PUBLIC_REBROWSE_API_BASE_URL) as string;
