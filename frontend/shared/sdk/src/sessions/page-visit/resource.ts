import ky from 'ky-universal';
import type { DataResponse, GroupByResult } from '@rebrowse/types';
import { querystring, withCredentials } from 'utils';

import { jsonResponse } from '../../http';

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
    return jsonResponse<DataResponse<GroupByResult<GroupBy>>>(
      ky.get(`${baseURL}/v1/pages/count${searchQuery}`, withCredentials(rest))
    );
  }

  return { count };
};
