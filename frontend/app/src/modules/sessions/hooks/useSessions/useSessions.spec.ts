import { sandbox } from '@rebrowse/testing';
import { renderHook, act } from '@testing-library/react-hooks';
import { SessionApi } from 'api';
import { REBROWSE_SESSIONS, REBROWSE_SESSIONS_DTOS } from 'test/data';

import { useSessions } from './useSessions';

describe('useSessions', () => {
  it('Should correctly load more sessions', async () => {
    const searchSessionsStub = sandbox
      .stub(SessionApi, 'getSessions')
      .resolves(REBROWSE_SESSIONS_DTOS.slice(0, 1));

    const { result, waitFor } = renderHook(() =>
      useSessions([], 0, {
        filters: [{ id: '1', key: 'location.city', value: 'Maribor' }],
        dateRange: {
          from: new Date('04 Dec 1995 00:12:00 GMT'),
          to: undefined,
        },
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
        'location.city': 'eq:Maribor',
        createdAt: ['gte:1995-12-04T00:12:00.000Z'],
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
        'location.city': 'eq:Maribor',
        createdAt: [
          'gte:1995-12-04T00:12:00.000Z',
          `lte:${REBROWSE_SESSIONS[0].createdAt.toISOString()}`,
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
