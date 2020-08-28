export type UserRole = 'ADMIN' | 'STANDARD';

export type UserDTO = {
  id: string;
  email: string;
  role: UserRole;
  fullName: string;
  createdAt: string;
  updatedAt: string;
  organizationId: string;
  phoneNumber: string | null;
  phoneNumberVerified: boolean;
};

export type User = Omit<UserDTO, 'createdAt' | 'updatedAt'> & {
  createdAt: Date;
  updatedAt: Date;
};
