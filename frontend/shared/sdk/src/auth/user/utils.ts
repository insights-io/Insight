import { User, UserDTO } from '@insight/types';

export const mapUser = (user: User | UserDTO): User => {
  return {
    ...user,
    createdAt: new Date(user.createdAt),
    updatedAt: new Date(user.updatedAt),
  };
};
