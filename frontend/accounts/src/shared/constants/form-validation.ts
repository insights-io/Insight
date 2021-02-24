export const REQUIRED_VALIDATION = {
  required: 'Required',
} as const;

const EMAIL_PATTERN = {
  value: /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,4}$/i,
  message: 'Please enter a valid email address',
} as const;

export const EMAIL_VALIDATION = {
  ...REQUIRED_VALIDATION,
  pattern: EMAIL_PATTERN,
} as const;

export const PASSWORD_MIN_LENGTH = 8;

export const PASSWORD_VALIDATION = {
  ...REQUIRED_VALIDATION,
  minLength: {
    value: PASSWORD_MIN_LENGTH,
    message: `Password must be at least ${PASSWORD_MIN_LENGTH} characters long`,
  },
} as const;
