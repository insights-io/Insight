import { SearchBean, SessionDTO } from '@rebrowse/types';
import { addHours, subHours } from 'date-fns';
import { REBROWSE_SESSIONS_DTOS } from 'test/data';

import { filterByParam } from './core';

describe('filter', () => {
  describe('filterByParam', () => {
    it('date', () => {
      const session = REBROWSE_SESSIONS_DTOS[0];

      const filterByCreatedAt = (search: SearchBean<SessionDTO>) =>
        filterByParam(session, 'createdAt', search, (v: string) =>
          new Date(v).valueOf()
        );

      /* gt */
      expect(
        filterByCreatedAt({ createdAt: `gt:${session.createdAt}` })
      ).toBeFalsy();

      expect(
        filterByCreatedAt({
          createdAt: `gt:${subHours(new Date(), 1).toISOString()}`,
        })
      ).toBeTruthy();

      expect(
        filterByCreatedAt({
          createdAt: `gt:${addHours(new Date(), 1).toISOString()}`,
        })
      ).toBeFalsy();

      /* gte */
      expect(
        filterByCreatedAt({ createdAt: `gte:${session.createdAt}` })
      ).toBeTruthy();

      expect(
        filterByCreatedAt({
          createdAt: `gte:${subHours(new Date(), 1).toISOString()}`,
        })
      ).toBeTruthy();

      expect(
        filterByCreatedAt({
          createdAt: `gte:${addHours(new Date(), 1).toISOString()}`,
        })
      ).toBeFalsy();

      /* lt */
      expect(
        filterByCreatedAt({ createdAt: `lt:${session.createdAt}` })
      ).toBeFalsy();

      expect(
        filterByCreatedAt({
          createdAt: `lt:${subHours(new Date(), 1).toISOString()}`,
        })
      ).toBeFalsy();

      expect(
        filterByCreatedAt({
          createdAt: `lt:${addHours(new Date(), 1).toISOString()}`,
        })
      ).toBeTruthy();

      /* lte */
      expect(
        filterByCreatedAt({ createdAt: `lte:${session.createdAt}` })
      ).toBeTruthy();

      expect(
        filterByCreatedAt({
          createdAt: `lte:${subHours(new Date(), 1).toISOString()}`,
        })
      ).toBeFalsy();

      expect(
        filterByCreatedAt({
          createdAt: `lte:${addHours(new Date(), 1).toISOString()}`,
        })
      ).toBeTruthy();
    });
  });
});
