import { sandbox } from '@insight/testing';
import { renderHook } from '@testing-library/react-hooks';
import { SessionApi } from 'api';
import { INSIGHT_SESSION, INSIGHT_SESSION_DTO } from 'test/data';

import useSessions from './useSessions';

describe('useSessions', () => {
  it('Should correctly load more sessions', async () => {
    const searchSessionsStub = sandbox
      .stub(SessionApi, 'getSessions')
      .resolves([INSIGHT_SESSION_DTO]);

    const { result, waitForNextUpdate } = renderHook(() =>
      useSessions([], 0, {
        filters: [{ id: '1', key: 'location.city', value: 'Maribor' }],
        dateRange: {
          from: new Date('04 Dec 1995 00:12:00 GMT'),
          to: undefined,
        },
      })
    );

    expect(result.current.count).toEqual(0);
    expect(result.current.data).toEqual([]);

    result.current.loadMoreItems(0, 0);
    sandbox.assert.calledWithExactly(searchSessionsStub, {
      search: {
        limit: 1,
        sort_by: ['-created_at'],
        'location.city': 'eq:Maribor',
        created_at: ['gte:1995-12-04T00:12:00.000Z'],
      },
    });

    await waitForNextUpdate();

    expect(result.current.data).toEqual([INSIGHT_SESSION]);

    result.current.loadMoreItems(1, 1);
    sandbox.assert.calledWithExactly(searchSessionsStub, {
      search: {
        limit: 1,
        sort_by: ['-created_at'],
        'location.city': 'eq:Maribor',
        created_at: [
          'gte:1995-12-04T00:12:00.000Z',
          `lte:${INSIGHT_SESSION.createdAt.toISOString()}`,
        ],
      },
    });

    await waitForNextUpdate();
    expect(result.current.data).toEqual([INSIGHT_SESSION, INSIGHT_SESSION]);
  });
});
