export type PasswordResetRequest = {
  email: string;
  token: string;
  org: string;
};

export type PasswordReset = PasswordResetRequest & {
  password: string;
};

export type ChangePasswordDTO = {
  currentPassword: string;
  newPassword: string;
  confirmNewPassword: string;
};
