import { REQUIRED_VALIDATION } from './base';

const EMAIL_PATTERN = {
  value: /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,4}$/i,
  message: 'Please enter a valid email address',
};

export const EMAIL_VALIDATION = {
  ...REQUIRED_VALIDATION,
  pattern: EMAIL_PATTERN,
};

export const EMAIL_PLACEHOLDER = 'john.doe@gmail.com';
export const WORK_EMAIL_PLACEHOLDER = 'john.doe@company.com';
