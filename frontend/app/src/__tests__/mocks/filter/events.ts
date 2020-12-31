import type { EventSeachBean, EventSearchQueryParams } from '@rebrowse/sdk';
import type { BrowserEventDTO } from '@rebrowse/types';
import { REBROWSE_EVENTS } from '__tests__/data/events';
import { httpOkResponse } from '__tests__/utils/request';

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

export const searchEventsMockImplementation = <
  GroupBy extends (keyof EventSearchQueryParams)[]
>(
  events: BrowserEventDTO[] = REBROWSE_EVENTS,
  search: EventSeachBean<GroupBy> = {}
) => {
  return Promise.resolve(
    httpOkResponse(events.filter((event) => filterBrowserEvent(event, search)))
  );
};
