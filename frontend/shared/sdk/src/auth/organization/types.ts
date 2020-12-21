import type { SearchBean, TeamInviteDTO, UserDTO } from '@rebrowse/types';
import type { RequestOptions } from 'types';

export type MemberSearchBean<
  GroupBy extends (keyof UserDTO)[] = []
> = SearchBean<UserDTO, GroupBy>;

export type MembersSearchOptions<GroupBy extends (keyof UserDTO)[] = []> = Omit<
  RequestOptions,
  'searchParams'
> & {
  search?: MemberSearchBean<GroupBy>;
};

export type TeamInviteSearchBean<
  GroupBy extends (keyof TeamInviteDTO)[] = []
> = SearchBean<TeamInviteDTO, GroupBy>;

export type TeamInviteSearchOptions<
  GroupBy extends (keyof TeamInviteDTO)[] = []
> = Omit<RequestOptions, 'searchParams'> & {
  search?: TeamInviteSearchBean<GroupBy>;
};
