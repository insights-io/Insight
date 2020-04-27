const EMAIL_PATTERN = {
  value: /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,4}$/i,
  message: 'Please enter a valid email address',
};

export const EMAIL_VALIDATION = {
  required: 'Required',
  pattern: EMAIL_PATTERN,
};
