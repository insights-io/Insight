export type OrganizationDTO = {
  id: string;
  name: string;
  createdAt: string;
};

export type Organization = Omit<OrganizationDTO, 'createdAt'> & {
  createdAt: Date;
};
