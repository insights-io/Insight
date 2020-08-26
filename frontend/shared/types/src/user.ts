export type UserRole = 'ADMIN' | 'STANDARD';

export type UserDTO = {
  id: string;
  email: string;
  role: UserRole;
  fullName: string;
  createdAt: string;
  organizationId: string;
  phoneNumber: string | null;
};

export type User = Omit<UserDTO, 'createdAt'> & {
  createdAt: Date;
};
