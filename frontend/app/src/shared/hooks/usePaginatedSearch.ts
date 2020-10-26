import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import debounce from 'lodash/debounce';
import { SortDirection } from '@insight/sdk';

import { usePrevious } from './usePrevious';

type SearchData<T> = {
  count: number;
  items: T[];
};

type SearchResult<T> = T[];

export type OnSearch<T> = {
  query: string;
  page: number;
  items: T[];
  direction: SortDirection;
};

type UserPaginatedSearchConfig<T> = {
  numItemsPerPage?: number;
  onMount?: boolean;
  wait?: number;
  search: (data: OnSearch<T>) => Promise<SearchResult<T>>;
  getCount: (data: OnSearch<T>) => Promise<number>;
  initialData?: SearchData<T>;
};

const INITIAL_DATA = { count: 0, items: [] };

export const usePaginatedSearch = <T>({
  numItemsPerPage = 20,
  wait = 350,
  onMount,
  search,
  getCount,
  initialData = INITIAL_DATA,
}: UserPaginatedSearchConfig<T>) => {
  const isMounted = useRef(false);
  const [query, setQuery] = useState('');
  const [page, setPage] = useState(1);
  const previousPage = usePrevious(page);

  const [items, setItems] = useState(initialData.items);
  const [count, setCount] = useState(initialData.count);
  const [isSearching, setIsSearching] = useState(false);

  const searchItems = useMemo(
    () =>
      debounce(
        (
          query: string,
          currentItems: T[],
          previousPage: number,
          page: number
        ) => {
          setIsSearching(true);
          search({
            query,
            page,
            items: currentItems,
            direction:
              page >= previousPage ? SortDirection.ASC : SortDirection.DESC,
          })
            .then(setItems)
            .finally(() => setIsSearching(false));
        },
        wait
      ),
    [search, wait]
  );

  const searchCount = useMemo(
    () =>
      debounce(
        (
          query: string,
          currentItems: T[],
          previousPage: number,
          page: number
        ) => {
          getCount({
            query,
            page,
            items: currentItems,
            direction:
              page >= previousPage ? SortDirection.ASC : SortDirection.DESC,
          }).then(setCount);
        },
        wait
      ),
    [getCount, wait]
  );

  useEffect(() => {
    if (onMount || isMounted.current) {
      searchCount(query, items, previousPage, page);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [query]);

  useEffect(() => {
    if (onMount || isMounted.current) {
      searchItems(query, items, previousPage, page);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [query, page]);

  const numPages = useMemo(
    () => (count === 0 ? 1 : Math.ceil(count / numItemsPerPage)),
    [count, numItemsPerPage]
  );

  const onPageChange = useCallback(
    (nextPage: number) => {
      setPage(Math.min(Math.max(nextPage, 1), numPages));
    },
    [numPages]
  );

  isMounted.current = true;

  return {
    query,
    setQuery,
    page,
    onPageChange,
    numPages,
    items,
    count,
    isSearching,
  };
};
