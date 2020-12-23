import type {
  SessionSearchBean,
  SessionSearchQueryParams,
} from '@rebrowse/sdk';
import { mockApiError } from '@rebrowse/storybook';
import { SessionDTO, TimePrecision } from '@rebrowse/types';
import { startOfDay } from 'date-fns';
import get from 'lodash/get';
import { REBROWSE_SESSIONS_DTOS } from '__tests__/data/sessions';

import { countBy, filterByParam, getParsedValue } from './core';

export const filterSession = <
  GroupBy extends (keyof SessionSearchQueryParams)[]
>(
  value: SessionDTO,
  search: SessionSearchBean<GroupBy> | undefined
) => filterByParam(value, search);

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

export const getDistinctMockImplementation = (
  on: keyof SessionSearchQueryParams,
  sessions: SessionDTO[] = REBROWSE_SESSIONS_DTOS
) => {
  return Promise.resolve([
    ...new Set(
      sessions
        .map((session) => getParsedValue(session, on) as string)
        .filter(Boolean)
    ),
  ]);
};
