import { sandbox } from '@rebrowse/testing';
import { SearchBean } from '@rebrowse/types';
import { act } from '@testing-library/react-hooks';
import { renderHook } from '__tests__/utils';

import { useResourceSearch } from './useResourceSearch';

type User = { createdAt: number; role: string };

const users: User[] = [
  { createdAt: 1, role: 'User' },
  { createdAt: 2, role: 'Admin' },
  { createdAt: 3, role: 'Owner' },
  { createdAt: 4, role: 'Billing' },
  { createdAt: 5, role: 'Management' },
];

describe('useResourceSearch', () => {
  test('As a user I should be able to search & navigate paginated resource', async () => {
    const numItemsPerPage = 2;

    const initialData = {
      items: users.slice(0, numItemsPerPage),
      count: users.length,
    };

    const filterByCreatedAt = (user: User, createdAt?: string) => {
      if (!createdAt) {
        return true;
      }

      const [direction, ...rest] = createdAt.split(':') as [
        'gt' | 'lt',
        string
      ];
      const value = parseInt(rest[0], 10);

      return direction === 'gt'
        ? user.createdAt > value
        : user.createdAt < value;
    };

    const search = sandbox
      .stub()
      .callsFake(
        async ({ query, createdAt }: SearchBean<{ createdAt?: string }>) => {
          return users
            .filter((user) =>
              query
                ? user.role.toLowerCase().includes(query.toLowerCase())
                : true
            )
            .filter((user) => filterByCreatedAt(user, createdAt as string))
            .slice(0, numItemsPerPage);
        }
      );

    const searchCount = sandbox
      .stub()
      .callsFake(async ({ query }: SearchBean<never>) => {
        return users.filter((user) =>
          query ? user.role.toLowerCase().includes(query.toLowerCase()) : true
        ).length;
      });

    const { result, waitFor } = renderHook(() =>
      useResourceSearch({
        numItemsPerPage,
        search,
        field: 'createdAt',
        resource: 'users',
        searchCount,
        initialData,
        debounce: 0,
      })
    );

    expect(result.current.items).toEqual(initialData.items);
    expect(result.current.count).toEqual(initialData.count);
    expect(result.current.numPages).toEqual(3);

    act(() => {
      result.current.setQuery('E');
    });

    await waitFor(() => {
      sandbox.assert.calledWithExactly(search, {
        query: 'E',
        limit: numItemsPerPage,
        sortBy: ['+createdAt'],
      });

      sandbox.assert.calledWithExactly(searchCount, { query: 'E' });
    });

    expect(result.current.items).toEqual([users[0], users[2]]);
    expect(result.current.count).toEqual(3);
    expect(result.current.numPages).toEqual(2);

    act(() => {
      result.current.onPageChange(2);
    });

    sandbox.assert.calledWithExactly(search, {
      query: 'E',
      limit: numItemsPerPage,
      sortBy: ['+createdAt'],
      createdAt: `gt:${users[2].createdAt}`,
    });

    sandbox.assert.calledOnce(searchCount);

    await waitFor(() => {
      expect(result.current.items).toEqual([users[4]]);
      expect(result.current.count).toEqual(3);
      expect(result.current.numPages).toEqual(2);
      expect(result.current.page).toEqual(2);
    });

    act(() => {
      result.current.setQuery('Er');
    });

    await waitFor(() => {
      expect(result.current.page).toEqual(1);
      expect(result.current.count).toEqual(2);
      expect(result.current.numPages).toEqual(1);
      expect(result.current.items).toEqual([users[0], users[2]]);
    });

    sandbox.assert.calledWithExactly(search, {
      query: 'Er',
      limit: numItemsPerPage,
      sortBy: ['+createdAt'],
    });

    sandbox.assert.calledWithExactly(searchCount, {
      query: 'Er',
    });
  });
});
