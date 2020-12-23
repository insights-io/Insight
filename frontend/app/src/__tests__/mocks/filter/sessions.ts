import type {
  SessionSearchBean,
  SessionSearchQueryParams,
} from '@rebrowse/sdk';
import { mockApiError } from '@rebrowse/storybook';
import { SessionDTO, TimePrecision } from '@rebrowse/types';
import { startOfDay } from 'date-fns';
import get from 'lodash/get';
import { REBROWSE_SESSIONS_DTOS } from '__tests__/data/sessions';

import { countBy, filterByParam } from './core';

export const filterSession = <
  GroupBy extends (keyof SessionSearchQueryParams)[]
>(
  value: SessionDTO,
  search: SessionSearchBean<GroupBy> | undefined
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

export const countSessionsBy = <
  GroupBy extends (keyof SessionSearchQueryParams)[] = []
>(
  data: SessionDTO[],
  search: SessionSearchBean<GroupBy> | undefined
) => {
  return countBy(
    data,
    (s) => filterSession(s, search),
    search,
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

export const retrieveSessionMockImplementation = (
  id: string,
  sessions: SessionDTO[] = REBROWSE_SESSIONS_DTOS
) => {
  const maybeSession = sessions.find((s) => s.id === id);
  if (maybeSession) {
    return Promise.resolve(maybeSession);
  }
  return Promise.reject(
    mockApiError({
      statusCode: 404,
      message: 'Not Found',
      reason: 'Not Found',
    })
  );
};
