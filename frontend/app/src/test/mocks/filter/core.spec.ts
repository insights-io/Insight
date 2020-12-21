import { SearchBean, SessionDTO } from '@rebrowse/types';
import { addHours, subHours } from 'date-fns';
import { REBROWSE_SESSIONS, REBROWSE_SESSIONS_DTOS } from 'test/data';

import { filterByParam } from './core';

describe('filter', () => {
  describe('filterByParam', () => {
    it('date', () => {
      const sessionDto = REBROWSE_SESSIONS_DTOS[0];
      const session = REBROWSE_SESSIONS[0];

      const filterByCreatedAt = (search: SearchBean<SessionDTO>) =>
        filterByParam(sessionDto, 'createdAt', search, (v: string) =>
          new Date(v).valueOf()
        );

      /* gt */
      expect(
        filterByCreatedAt({ createdAt: `gt:${sessionDto.createdAt}` })
      ).toBeFalsy();

      expect(
        filterByCreatedAt({
          createdAt: `gt:${subHours(session.createdAt, 1).toISOString()}`,
        })
      ).toBeTruthy();

      expect(
        filterByCreatedAt({
          createdAt: `gt:${addHours(session.createdAt, 1).toISOString()}`,
        })
      ).toBeFalsy();

      /* gte */
      expect(
        filterByCreatedAt({ createdAt: `gte:${sessionDto.createdAt}` })
      ).toBeTruthy();

      expect(
        filterByCreatedAt({
          createdAt: `gte:${subHours(session.createdAt, 1).toISOString()}`,
        })
      ).toBeTruthy();

      expect(
        filterByCreatedAt({
          createdAt: `gte:${addHours(session.createdAt, 1).toISOString()}`,
        })
      ).toBeFalsy();

      /* lt */
      expect(
        filterByCreatedAt({ createdAt: `lt:${sessionDto.createdAt}` })
      ).toBeFalsy();

      expect(
        filterByCreatedAt({
          createdAt: `lt:${subHours(session.createdAt, 1).toISOString()}`,
        })
      ).toBeFalsy();

      expect(
        filterByCreatedAt({
          createdAt: `lt:${addHours(session.createdAt, 1).toISOString()}`,
        })
      ).toBeTruthy();

      /* lte */
      expect(
        filterByCreatedAt({ createdAt: `lte:${sessionDto.createdAt}` })
      ).toBeTruthy();

      expect(
        filterByCreatedAt({
          createdAt: `lte:${subHours(session.createdAt, 1).toISOString()}`,
        })
      ).toBeFalsy();

      expect(
        filterByCreatedAt({
          createdAt: `lte:${addHours(session.createdAt, 1).toISOString()}`,
        })
      ).toBeTruthy();
    });
  });
});
