import type { SearchBean, TeamInviteDTO, UserDTO } from '@rebrowse/types';
import type { RequestOptions } from 'types';

export type MemberSearchBean = SearchBean<UserDTO>;

export type MembersSearchOptions = Omit<RequestOptions, 'searchParams'> & {
  search?: MemberSearchBean;
};

export type TeamInviteSearchBean = SearchBean<TeamInviteDTO>;

export type TeamInviteSearchOptions = Omit<RequestOptions, 'searchParams'> & {
  search?: TeamInviteSearchBean;
};
