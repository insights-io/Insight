import type { EventSeachBean } from '@rebrowse/sdk';
import type { BrowserEventDTO } from '@rebrowse/types';

import { filterByParam } from './core';

export const filterBrowserEvent = (
  event: BrowserEventDTO,
  search: EventSeachBean | undefined
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
