import { UserRole } from './user';

export type AvatarDTO =
  | {
      type: 'initials';
    }
  | {
      type: 'avatar';
      image: string;
    };

export type AvatarType = AvatarDTO['type'];

export type OrganizationDTO = {
  id: string;
  name: string | undefined;
  openMembership: boolean;
  avatar?: AvatarDTO;
  defaultRole: UserRole;
  createdAt: string;
  updatedAt: string;
};

export type Organization = Omit<OrganizationDTO, 'createdAt' | 'updatedAt'> & {
  createdAt: Date;
  updatedAt: Date;
};

export type TeamInviteCreateDTO = {
  email: string;
  role: UserRole;
};

export type TeamInviteDTO = TeamInviteCreateDTO & {
  token: string;
  org: string;
  creator: string;
  createdAt: string;
};

export type TeamInvite = Omit<TeamInviteDTO, 'createdAt'> & {
  createdAt: Date;
};

export type PasswordPolicy = {
  minCharacters: number;
  preventPasswordReuse: boolean;
  requireUppercaseCharacter: boolean;
  requireLowercaseCharacter: boolean;
  requireNumber: boolean;
  requireNonAlphanumericCharacter: boolean;
};

export type PasswordPolicyCreateParams = PasswordPolicy;
export type PasswordPolicyUpdateParams = Partial<PasswordPolicy>;

export type OrganizationPasswordPolicyDTO = PasswordPolicy & {
  organizationId: string;
  updatedAt: string;
  createdAt: string;
};

export type OrganizationPasswordPolicy = Omit<
  OrganizationPasswordPolicyDTO,
  'updatedAt' | 'createdAt'
> & {
  updatedAt: Date;
  createdAt: Date;
};
