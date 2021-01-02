import type { GroupByResult } from '@rebrowse/types';
import { querystring } from 'utils';

import { HttpClient, jsonDataResponse } from '../../http';

import type {
  PageVisitSearchRequestOptions,
  PageVisitSearchQueryParams,
} from './types';

export const pageVisitResource = (
  client: HttpClient,
  sessionApiBaseUrl: string
) => {
  return {
    count: <GroupBy extends (keyof PageVisitSearchQueryParams)[]>({
      baseUrl = sessionApiBaseUrl,
      search,
      ...requestOptions
    }: PageVisitSearchRequestOptions<GroupBy> = {}) => {
      const searchQuery = querystring(search);
      return jsonDataResponse<GroupByResult<GroupBy>>(
        client.get(`${baseUrl}/v1/pages/count${searchQuery}`, requestOptions)
      );
    },
  };
};
