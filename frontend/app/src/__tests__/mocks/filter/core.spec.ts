import { TermCondition } from '@rebrowse/sdk';
import { SearchBean, SessionDTO } from '@rebrowse/types';
import { addHours, subHours } from 'date-fns';
import { REBROWSE_SESSIONS, REBROWSE_SESSIONS_DTOS } from '__tests__/data';

import { filterByParam } from './core';

describe('filter', () => {
  describe('filterByParam', () => {
    it('createdAt | Date', () => {
      const sessionDto = REBROWSE_SESSIONS_DTOS[0];
      const session = REBROWSE_SESSIONS[0];

      const filterByCreatedAt = (search: SearchBean<SessionDTO>) =>
        filterByParam(sessionDto, 'createdAt', search, (v: string) =>
          new Date(v).valueOf()
        );

      /* gt */
      expect(
        filterByCreatedAt({ createdAt: TermCondition.GT(session.createdAt) })
      ).toBeFalsy();

      expect(
        filterByCreatedAt({
          createdAt: TermCondition.GT(subHours(session.createdAt, 1)),
        })
      ).toBeTruthy();

      expect(
        filterByCreatedAt({
          createdAt: TermCondition.GT(addHours(session.createdAt, 1)),
        })
      ).toBeFalsy();

      /* gte */
      expect(
        filterByCreatedAt({ createdAt: TermCondition.GTE(session.createdAt) })
      ).toBeTruthy();

      expect(
        filterByCreatedAt({
          createdAt: TermCondition.GTE(subHours(session.createdAt, 1)),
        })
      ).toBeTruthy();

      expect(
        filterByCreatedAt({
          createdAt: TermCondition.GTE(addHours(session.createdAt, 1)),
        })
      ).toBeFalsy();

      /* lt */
      expect(
        filterByCreatedAt({ createdAt: TermCondition.LT(session.createdAt) })
      ).toBeFalsy();

      expect(
        filterByCreatedAt({
          createdAt: TermCondition.LT(subHours(session.createdAt, 1)),
        })
      ).toBeFalsy();

      expect(
        filterByCreatedAt({
          createdAt: TermCondition.LT(addHours(session.createdAt, 1)),
        })
      ).toBeTruthy();

      /* lte */
      expect(
        filterByCreatedAt({
          createdAt: TermCondition.LTE(sessionDto.createdAt),
        })
      ).toBeTruthy();

      expect(
        filterByCreatedAt({
          createdAt: TermCondition.LTE(subHours(session.createdAt, 1)),
        })
      ).toBeFalsy();

      expect(
        filterByCreatedAt({
          createdAt: TermCondition.LTE(addHours(session.createdAt, 1)),
        })
      ).toBeTruthy();
    });
  });
});
