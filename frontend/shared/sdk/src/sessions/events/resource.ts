import type { BrowserEventDTO } from '@rebrowse/types';

import { querystring } from '../../utils';
import { HttpClient, jsonDataResponse } from '../../http';

import type {
  EventSearchRequestOptions,
  EventSearchQueryParams,
} from './types';

export const eventsResource = (
  client: HttpClient,
  sessionApiBaseUrl: string
) => {
  return {
    search: <GroupBy extends (keyof EventSearchQueryParams)[] = []>(
      sessionId: string,
      {
        baseUrl = sessionApiBaseUrl,
        search,
        ...requestOptions
      }: EventSearchRequestOptions<GroupBy> = {}
    ) => {
      const query = decodeURIComponent(querystring(search));
      return jsonDataResponse<BrowserEventDTO[]>(
        client.get(
          `${baseUrl}/v1/sessions/${sessionId}/events/search${query}`,
          requestOptions
        )
      );
    },
  };
};
