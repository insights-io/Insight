export const INDEX_ROUTE = '/';

/* Sign up */
export const SIGNUP_ROUTE = '/signup';
export const SIGNUP_CONFIRM_ROUTE = '/signup-confirm';

/* Password */
export const PASSWORD_FORGOT_ROUTE = '/password-forgot';
export const PASSWORD_RESET_ROUTE = '/password-reset';

/* Sign In */
export const SIGNIN_ROUTE = '/signin';
const SIGNING_CHALLENGE_ROUTE = `${SIGNIN_ROUTE}/challenge`;
export const SIGNIN_PWD_CHALLENGE_ROUTE = `${SIGNING_CHALLENGE_ROUTE}/pwd`;
export const SIGNIN_MFA_CHALLENGE_ROUTE = `${SIGNING_CHALLENGE_ROUTE}/mfa`;

/* Query params */
export const LOGIN_HINT_QUERY = 'login_hint';
export const REDIRECT_QUERY = 'redirect';

/* Cookies */
export const PWD_CHALLENGE_SESSION_ID = 'AuthorizationPwdChallengeSessionId';
export const MFA_CHALLENGE_SESSION_ID = 'AuthorizationMfaChallengeSessionId';
export const SESSION_ID = 'SessionId';
