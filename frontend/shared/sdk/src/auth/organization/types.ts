import type { SearchBean } from '@insight/types';
import type { RequestOptions } from 'core';

export type MembersSearchOptions = Omit<RequestOptions, 'searchParams'> & {
  search?: SearchBean;
};
