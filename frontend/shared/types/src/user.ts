export type UserRole = 'ADMIN' | 'STANDARD';

export type UserDTO = {
  id: string;
  email: string;
  role: UserRole;
};
