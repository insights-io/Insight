import type { EventSeachBean, EventSearchQueryParams } from '@rebrowse/sdk';
import type { BrowserEventDTO, SearchBean } from '@rebrowse/types';
import { REBROWSE_EVENTS } from '__tests__/data/events';

import { filterByParam } from './core';

export const filterBrowserEvent = <
  GroupBy extends (keyof EventSearchQueryParams)[]
>(
  event: BrowserEventDTO,
  search: EventSeachBean<GroupBy> | undefined
) => {
  if (!search) {
    return true;
  }

  // eslint-disable-next-line no-restricted-syntax
  for (const key of ['event.e']) {
    if (
      !filterByParam(
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        event as any,
        key,
        search,
        (v) => parseInt(v, 10),
        (r, f) => r[(f as string).replace('event.', '')]
      )
    ) {
      return false;
    }
  }

  return true;
};

export const searchEventsMockImplementation = (
  search: SearchBean<EventSearchQueryParams, 'event.e'[]> | undefined,
  events: BrowserEventDTO[] = REBROWSE_EVENTS
) => {
  return Promise.resolve(events.filter((e) => filterBrowserEvent(e, search)));
};
