export const PASSWORD_MIN_LENGTH = 8;

export const PASSWORD_VALIDATION = {
  required: 'Required',
  minLength: {
    value: PASSWORD_MIN_LENGTH,
    message: `Password must be at least ${PASSWORD_MIN_LENGTH} characters long`,
  },
};
