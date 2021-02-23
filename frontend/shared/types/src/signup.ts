import type { PhoneNumber } from './user';

export type SignUpRequestDTO = {
  redirect: string;
  fullName: string;
  company: string;
  email: string;
  password: string;
  phoneNumber?: PhoneNumber;
};
