import { PasswordResetRequest } from './password';

export type SignupRequest = PasswordResetRequest;

export type Signup = SignupRequest & {
  password: string;
};
