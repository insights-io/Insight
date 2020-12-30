import ky from 'ky-universal';
import type { GroupByResult } from '@rebrowse/types';
import { querystring, withCredentials } from 'utils';

import { jsonDataResponse } from '../../http';

import type {
  PageVisitSearchRequestOptions,
  PageVisitSearchQueryParams,
} from './types';

export const createPageVisitClient = (sessionApiBaseUrl: string) => {
  function count<GroupBy extends (keyof PageVisitSearchQueryParams)[]>({
    baseURL = sessionApiBaseUrl,
    search,
    ...rest
  }: PageVisitSearchRequestOptions<GroupBy> = {}) {
    const searchQuery = querystring(search);
    return jsonDataResponse<GroupByResult<GroupBy>>(
      ky.get(`${baseURL}/v1/pages/count${searchQuery}`, withCredentials(rest))
    );
  }

  return { count };
};
