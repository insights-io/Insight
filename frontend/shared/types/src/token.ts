export type AuthTokenDTO = {
  token: string;
  userId: string;
  createdAt: string;
};

export type AuthToken = Omit<AuthTokenDTO, 'createdAt'> & {
  createdAt: Date;
};
