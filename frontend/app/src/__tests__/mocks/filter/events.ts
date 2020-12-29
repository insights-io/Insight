import type {
  EventSeachBean,
  EventSearchQueryParams,
  HttpResponse,
} from '@rebrowse/sdk';
import type {
  BrowserEventDTO,
  DataResponse,
  SearchBean,
} from '@rebrowse/types';
import { REBROWSE_EVENTS } from '__tests__/data/events';

import { filterByParam } from './core';

export const filterBrowserEvent = <
  GroupBy extends (keyof EventSearchQueryParams)[]
>(
  event: BrowserEventDTO,
  search: EventSeachBean<GroupBy> = {}
) => {
  const mappedSearch = Object.keys(search).reduce((acc, key) => {
    const mappedKey = key.replace('event.', '');
    return { ...acc, [mappedKey]: search[key as keyof typeof search] };
  }, {} as typeof search);

  return filterByParam(
    (event as unknown) as Record<string, unknown>,
    mappedSearch
  );
};

export const searchEventsMockImplementation = (
  search: SearchBean<EventSearchQueryParams, 'event.e'[]> | undefined,
  events: BrowserEventDTO[] = REBROWSE_EVENTS
): Promise<HttpResponse<DataResponse<BrowserEventDTO[]>>> => {
  return Promise.resolve({
    data: { data: events.filter((e) => filterBrowserEvent(e, search)) },
    statusCode: 200,
    headers: new Headers(),
  });
};
