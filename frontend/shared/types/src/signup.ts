import type { PhoneNumber } from './user';

export type SignUpRequestDTO = {
  fullName: string;
  company: string;
  email: string;
  password: string;
  phoneNumber?: PhoneNumber;
};
