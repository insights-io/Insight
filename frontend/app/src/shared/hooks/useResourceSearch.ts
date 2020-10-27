import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import debounce from 'lodash/debounce';
import { SortDirection } from '@insight/sdk';
import type { SearchBean } from '@insight/types';

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

type UserPaginatedSearchConfig<
  Key extends keyof Item,
  Item extends Record<string, unknown>
> = {
  field: Key;
  numItemsPerPage?: number;
  onMount?: boolean;
  wait?: number;
  search: (search: SearchBean) => Promise<SearchResult<Item>>;
  searchCount: (search: SearchBean) => Promise<number>;
  initialData?: SearchData<Item>;
};

const INITIAL_DATA = { count: 0, items: [] };

export const useResourceSearch = <
  Key extends keyof Item,
  Item extends Record<string, unknown>
>({
  field,
  numItemsPerPage = 20,
  wait = 350,
  onMount,
  search,
  searchCount,
  initialData = INITIAL_DATA,
}: UserPaginatedSearchConfig<Key, Item>) => {
  const isMounted = useRef(false);
  const [query, setQuery] = useState('');
  const [page, setPage] = useState(1);
  const previousPage = usePrevious(page);

  const [items, setItems] = useState(initialData.items);
  const [count, setCount] = useState(initialData.count);
  const [isSearching, setIsSearching] = useState(false);

  const createSearchBean = useCallback(
    (query: string, page: number, previousPage: number, items: Item[]) => {
      const search: SearchBean = {};
      if (query) {
        search.query = query;
      }

      if (page > 1) {
        const fieldName = field as string;
        if (page >= previousPage) {
          const lastItem = items[items.length - 1];
          search[fieldName] = `gt:${lastItem[fieldName]}`;
        } else {
          const firstItem = items[0];
          search[fieldName] = `lt:${firstItem[fieldName]}`;
        }
      }

      return search;
    },
    [field]
  );

  const searchItems = useMemo(
    () =>
      debounce(
        (query: string, items: Item[], previousPage: number, page: number) => {
          const searchBean = createSearchBean(query, page, previousPage, items);
          setIsSearching(true);
          let sortDirection = SortDirection.ASC;
          if (page > 1 && page < previousPage) {
            sortDirection = SortDirection.DESC;
          }

          searchBean.limit = numItemsPerPage;
          searchBean.sort_by = [`${sortDirection}${field}`];

          search(searchBean)
            .then(setItems)
            .finally(() => setIsSearching(false));
        },
        wait
      ),
    [search, wait, numItemsPerPage, createSearchBean, field]
  );

  const onSearchCount = useMemo(
    () =>
      debounce(
        (query: string, items: Item[], previousPage: number, page: number) => {
          const searchBean = createSearchBean(query, page, previousPage, items);
          searchCount(searchBean).then(setCount);
        },
        wait
      ),
    [searchCount, wait, createSearchBean]
  );

  useEffect(() => {
    if (onMount || isMounted.current) {
      onSearchCount(query, items, previousPage, page);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [query]);

  useEffect(() => {
    if (onMount || isMounted.current) {
      searchItems(query, items, previousPage, page);
    } else {
      isMounted.current = true;
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

  const revalidate = () => {
    onSearchCount(query, items, previousPage, page);
    searchItems(query, items, previousPage, page);
  };

  return {
    query,
    setQuery,
    page,
    onPageChange,
    numPages,
    items,
    count,
    isSearching,
    revalidate,
  };
};
