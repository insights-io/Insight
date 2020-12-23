import {
  EventSearchQueryParams,
  SessionSearchBean,
  TermCondition,
} from '@rebrowse/sdk';
import { SearchBean, SessionDTO } from '@rebrowse/types';
import { addHours, subHours } from 'date-fns';
import {
  REBROWSE_SESSIONS,
  REBROWSE_SESSIONS_DTOS,
  CONSOLE_EVENTS,
} from '__tests__/data';

import { filterByParam } from './core';
import { filterBrowserEvent } from './events';
import { filterSession } from './sessions';

describe('filter', () => {
  describe('filterByParam', () => {
    it('events > "event.e" | Number', () => {
      const [event] = CONSOLE_EVENTS;
      const { e } = event;

      const filterEventByParam = (search: SearchBean<EventSearchQueryParams>) =>
        filterBrowserEvent(event, search);

      expect(
        filterEventByParam({ 'event.e': TermCondition.EQ(e) })
      ).toBeTruthy();

      expect(
        filterEventByParam({ 'event.e': TermCondition.GT(e) })
      ).toBeFalsy();

      expect(
        filterEventByParam({ 'event.e': TermCondition.GTE(e) })
      ).toBeTruthy();

      expect(
        filterEventByParam({ 'event.e': TermCondition.LT(e) })
      ).toBeFalsy();

      expect(
        filterEventByParam({ 'event.e': TermCondition.LTE(e) })
      ).toBeTruthy();

      expect(
        filterEventByParam({
          'event.e': [TermCondition.GTE(e), TermCondition.LTE(e + 1)],
        })
      ).toBeTruthy();
    });

    it('sessions > "user_agent.agent_name" | String', () => {
      const [
        {
          userAgent: { agentName },
        },
      ] = REBROWSE_SESSIONS;
      const filter = (search: SessionSearchBean) =>
        filterSession(REBROWSE_SESSIONS_DTOS[0], search);

      expect(
        filter({ 'userAgent.agent_name': TermCondition.EQ('random') })
      ).toBeFalsy();

      expect(
        filter({ 'userAgent.agent_name': TermCondition.EQ(agentName) })
      ).toBeTruthy();

      expect(
        filter({ 'userAgent.agent_name': TermCondition.GT(agentName) })
      ).toBeFalsy();

      expect(
        filter({ 'userAgent.agent_name': TermCondition.GTE(agentName) })
      ).toBeTruthy();

      expect(
        filter({ 'userAgent.agent_name': TermCondition.LT(agentName) })
      ).toBeFalsy();

      expect(
        filter({ 'userAgent.agent_name': TermCondition.LTE(agentName) })
      ).toBeTruthy();
    });

    it('sessions > "createdAt" | Date', () => {
      const [{ createdAt }] = REBROWSE_SESSIONS;

      const filter = (search: SearchBean<SessionDTO>) =>
        filterByParam(REBROWSE_SESSIONS_DTOS[0], search);

      /* gt */
      expect(filter({ createdAt: TermCondition.GT(createdAt) })).toBeFalsy();

      expect(
        filter({ createdAt: TermCondition.GT(subHours(createdAt, 1)) })
      ).toBeTruthy();

      expect(
        filter({ createdAt: TermCondition.GT(addHours(createdAt, 1)) })
      ).toBeFalsy();

      /* gte */
      expect(filter({ createdAt: TermCondition.GTE(createdAt) })).toBeTruthy();

      expect(
        filter({ createdAt: TermCondition.GTE(subHours(createdAt, 1)) })
      ).toBeTruthy();

      expect(
        filter({ createdAt: TermCondition.GTE(addHours(createdAt, 1)) })
      ).toBeFalsy();

      /* lt */
      expect(filter({ createdAt: TermCondition.LT(createdAt) })).toBeFalsy();

      expect(
        filter({ createdAt: TermCondition.LT(subHours(createdAt, 1)) })
      ).toBeFalsy();

      expect(
        filter({ createdAt: TermCondition.LT(addHours(createdAt, 1)) })
      ).toBeTruthy();

      /* lte */
      expect(filter({ createdAt: TermCondition.LTE(createdAt) })).toBeTruthy();

      expect(
        filter({ createdAt: TermCondition.LTE(subHours(createdAt, 1)) })
      ).toBeFalsy();

      expect(
        filter({ createdAt: TermCondition.LTE(addHours(createdAt, 1)) })
      ).toBeTruthy();
    });
  });
});
