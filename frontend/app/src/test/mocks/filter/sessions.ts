import type { SessionSearchBean } from '@rebrowse/sdk';
import { SearchBean, SessionDTO, TimePrecision } from '@rebrowse/types';
import { startOfDay } from 'date-fns';
import get from 'lodash/get';

import { countBy, filterByParam } from './core';

export const filterSession = (
  value: SessionDTO,
  search: SessionSearchBean | undefined
) => {
  if (!search) {
    return true;
  }

  const {
    location: { city, countryName, continentName },
  } = value;

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

  if (search.createdAt) {
    if (
      !filterByParam(value, 'createdAt', { createdAt: search.createdAt }, (v) =>
        new Date(v).valueOf()
      )
    ) {
      return false;
    }
  }

  return true;
};

export const countSessionsBy = (
  data: SessionDTO[],
  search: SessionSearchBean | undefined
) => {
  return countBy(
    data,
    (s) => filterSession(s, search),
    search as SearchBean<SessionDTO>,
    (v, field) => {
      const value = get(v, field);
      if (search?.dateTrunc === TimePrecision.DAY && field === 'createdAt') {
        return startOfDay(
          new Date(value as string)
        ).toISOString() as SessionDTO[typeof field];
      }
      return value;
    }
  );
};
