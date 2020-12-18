import { EventSeachBean, SessionSearchBean } from '@rebrowse/sdk';
import { BrowserEventDTO, SessionDTO } from '@rebrowse/types';

export const filterBrowserEvent = (
  event: BrowserEventDTO,
  search: EventSeachBean | undefined
) => {
  if (!search) {
    return true;
  }

  // TODO: generalize logic for filtering on FE
  const { e } = event;

  if (search['event.e']) {
    const params = (Array.isArray(search['event.e'])
      ? search['event.e']
      : [search['event.e']]) as string[];

    // eslint-disable-next-line no-restricted-syntax
    for (const queryParam of params) {
      const [termCondition, rawValue] = queryParam.split(':');
      const value = parseInt(rawValue, 10);

      if (termCondition === 'gte' && e < value) {
        return false;
      }
      if (termCondition === 'lte' && e > value) {
        return false;
      }
      if (termCondition === 'eq' && e !== value) {
        return false;
      }
    }
  }

  return true;
};

export const filterSession = (
  s: SessionDTO,
  search: SessionSearchBean | undefined
) => {
  if (!search) {
    return true;
  }

  const {
    location: { city, countryName, continentName },
  } = s;

  if (search['location.city']) {
    if (`eq:${city}` !== search['location.city']) {
      return false;
    }
  }
  if (search['location.countryName']) {
    if (`eq:${countryName}` !== search['location.countryName']) {
      return false;
    }
  }

  if (search['location.continentName']) {
    if (`eq:${continentName}` !== search['location.continentName']) {
      return false;
    }
  }

  return true;
};
