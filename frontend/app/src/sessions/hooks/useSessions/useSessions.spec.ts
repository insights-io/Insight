import { TermCondition } from '@rebrowse/sdk';
import { sandbox } from '@rebrowse/testing';
import { renderHook, act } from '@testing-library/react-hooks';
import { SessionApi } from 'api';
import { REBROWSE_SESSIONS, REBROWSE_SESSIONS_DTOS } from '__tests__/data';

import { useSessions } from './useSessions';

describe('useSessions', () => {
  it('Should correctly load more sessions', async () => {
    const searchSessionsStub = sandbox
      .stub(SessionApi, 'getSessions')
      .resolves({
        data: { data: REBROWSE_SESSIONS_DTOS.slice(0, 1) },
        statusCode: 200,
        headers: new Headers(),
      });

    const from = new Date('04 Dec 1995 00:12:00 GMT');

    const { result, waitFor } = renderHook(() =>
      useSessions([], 0, {
        filters: [{ id: '1', key: 'location.city', value: 'Maribor' }],
        dateRange: { from, to: undefined },
      })
    );

    expect(result.current.count).toEqual(0);
    expect(result.current.sessions).toEqual([]);

    act(() => {
      result.current.loadMoreItems(0, 0);
    });

    sandbox.assert.calledWithExactly(searchSessionsStub, {
      search: {
        limit: 1,
        sortBy: ['-createdAt'],
        'location.city': TermCondition.EQ('Maribor'),
        createdAt: [TermCondition.GTE(from)],
      },
    });

    await waitFor(() => {
      expect(result.current.sessions).toEqual(REBROWSE_SESSIONS.slice(0, 1));
    });

    act(() => {
      result.current.loadMoreItems(1, 1);
    });

    sandbox.assert.calledWithExactly(searchSessionsStub, {
      search: {
        limit: 1,
        sortBy: ['-createdAt'],
        'location.city': TermCondition.EQ('Maribor'),
        createdAt: [
          TermCondition.GTE(from),
          TermCondition.LTE(REBROWSE_SESSIONS[0].createdAt),
        ],
      },
    });

    await waitFor(() => {
      expect(result.current.sessions).toEqual([
        REBROWSE_SESSIONS[0],
        REBROWSE_SESSIONS[0],
      ]);
    });
  });
});
