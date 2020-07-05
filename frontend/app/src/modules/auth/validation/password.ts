import { REQUIRED_VALIDATION } from './base';

export const PASSWORD_MIN_LENGTH = 8;

export const PASSWORD_VALIDATION = {
  ...REQUIRED_VALIDATION,
  minLength: {
    value: PASSWORD_MIN_LENGTH,
    message: `Password must be at least ${PASSWORD_MIN_LENGTH} characters long`,
  },
};
