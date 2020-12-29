import ky from 'ky-universal';
import type { DataResponse, BrowserEventDTO } from '@rebrowse/types';
import { querystring, withCredentials } from 'utils';

import { jsonResponse } from '../../http';

import type {
  EventSearchRequestOptions,
  EventSearchQueryParams,
} from './types';

export const createEventsClient = (sessionApiBaseUrl: string) => {
  return {
    search: <GroupBy extends (keyof EventSearchQueryParams)[] = []>(
      sessionId: string,
      {
        baseURL = sessionApiBaseUrl,
        search,
        ...rest
      }: EventSearchRequestOptions<GroupBy> = {}
    ) => {
      const query = decodeURIComponent(querystring(search));
      return jsonResponse<DataResponse<BrowserEventDTO[]>>(
        ky.get(
          `${baseURL}/v1/sessions/${sessionId}/events/search${query}`,
          withCredentials(rest)
        )
      );
    },
  };
};
