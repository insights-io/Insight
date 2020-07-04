export type PasswordResetRequest = {
  email: string;
  token: string;
  org: string;
};

export type PasswordReset = PasswordResetRequest & {
  password: string;
};

export type ChangePasswordDTO = {
  newPassword: string;
  confirmNewPassword: string;
};
