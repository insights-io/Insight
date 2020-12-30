import type {
  OrganizationDTO,
  SearchBean,
  TeamInviteDTO,
} from '@rebrowse/types';
import type { RequestOptions } from 'types';

export type TeamInviteSearchBean<
  GroupBy extends (keyof TeamInviteDTO)[] = []
> = SearchBean<TeamInviteDTO, GroupBy>;

export type TeamInviteSearchOptions<
  GroupBy extends (keyof TeamInviteDTO)[] = []
> = Omit<RequestOptions, 'searchParams'> & {
  search?: TeamInviteSearchBean<GroupBy>;
};

export type OrganizationUpdateParams = Partial<
  Pick<OrganizationDTO, 'name' | 'openMembership'>
>;
