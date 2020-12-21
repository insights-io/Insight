import ky from 'ky-universal';
import type { DataResponse, GroupByResult } from '@rebrowse/types';

import { getData, querystring, withCredentials } from '../utils';

import type {
  PageVisitSearchRequestOptions,
  PageVisitQueryParams,
} from './types';

export const createPagesClient = (sessionApiBaseUrl: string) => {
  function count<GroupBy extends (keyof PageVisitQueryParams)[]>({
    baseURL = sessionApiBaseUrl,
    search,
    ...rest
  }: PageVisitSearchRequestOptions<GroupBy> = {}) {
    const searchQuery = querystring(search);
    return ky
      .get(`${baseURL}/v1/pages/count${searchQuery}`, withCredentials(rest))
      .json<DataResponse<GroupByResult<GroupBy>>>()
      .then(getData);
  }

  const PagesApi = { count };

  return PagesApi;
};
