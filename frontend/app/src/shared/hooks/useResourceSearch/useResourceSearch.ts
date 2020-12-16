import { useCallback, useMemo, useState } from 'react';
import { SearchBean, SortDirection } from '@rebrowse/types';
import { usePaginatedQuery, useQueryCache } from 'react-query';
import { usePrevious } from 'shared/hooks/usePrevious';
import { useDebounce } from 'use-debounce';

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
  Resource extends string,
  Key extends keyof Item,
  Item extends Record<string, unknown>
> = {
  resource: Resource;
  field: Key;
  numItemsPerPage?: number;
  search: (search: SearchBean<Item>) => Promise<SearchResult<Item>>;
  searchCount: (search: SearchBean<Item>) => Promise<number>;
  initialData?: SearchData<Item>;
  debounce?: number;
};

const INITIAL_DATA = { count: 0, items: [] };

export const useResourceSearch = <
  Resource extends string,
  Key extends keyof Item,
  Item extends Record<string, unknown>
>({
  resource,
  field,
  numItemsPerPage = 20,
  search,
  searchCount,
  initialData = INITIAL_DATA,
  debounce = 250,
}: UserPaginatedSearchConfig<Resource, Key, Item>) => {
  const [query, setQuery] = useState('');
  const [page, setPage] = useState(1);
  const previousPage = usePrevious(page);
  const queryCache = useQueryCache();
  const [debouncedQuery] = useDebounce(query, debounce);

  const onQueryChange = (query: string) => {
    setQuery(query);
    setPage(1);
  };

  const isDefaultQuery = useMemo(() => page === 1 && debouncedQuery === '', [
    page,
    debouncedQuery,
  ]);

  const itemsCacheKey = useMemo(
    () => [resource, { page, query: debouncedQuery }] as const,
    [page, debouncedQuery, resource]
  );

  const createSearchQuery = () => {
    const searchBean: SearchBean<Item> = {};
    if (query) {
      searchBean.query = query;
    }

    return searchBean;
  };

  const {
    data: items = initialData.items,
    isFetching: isFetchingItems,
    refetch: refetchItems,
  } = usePaginatedQuery(
    itemsCacheKey,
    (...args: typeof itemsCacheKey) => {
      const items = queryCache.getQueryData<Item[]>(args);
      if (items) {
        return items;
      }

      const searchBean = createSearchQuery();
      const [key, { page }] = args;

      if (page > 1) {
        const previousCacheKey = [key, { page: previousPage, query }];
        const previousItems = queryCache.getQueryData<Item[]>(previousCacheKey);

        if (previousItems) {
          const fieldName = field as keyof Item;
          if (page >= previousPage) {
            const lastItem = previousItems[previousItems.length - 1];
            searchBean[
              fieldName
            ] = `gt:${lastItem[fieldName]}` as SearchBean<Item>[keyof Item];
          } else {
            const firstItem = previousItems[0];
            searchBean[
              fieldName
            ] = `lt:${firstItem[fieldName]}` as SearchBean<Item>[keyof Item];
          }
        }
      }

      searchBean.limit = numItemsPerPage;
      if (page > 1 && page < previousPage) {
        searchBean.sortBy = [`-${field}`];
      } else {
        searchBean.sortBy = [`+${field}`];
      }

      return search(searchBean);
    },
    { initialData: isDefaultQuery ? initialData.items : undefined }
  );

  const countCacheKey = useMemo(
    () => [`${resource}:count`, { query: debouncedQuery }],
    [debouncedQuery, resource]
  );

  const {
    data: count = initialData.count,
    isFetching: isFetchingCount,
    refetch: refetchCount,
  } = usePaginatedQuery(
    countCacheKey,
    (...args: typeof countCacheKey) => {
      const count = queryCache.getQueryData<number>(args);
      if (count !== undefined) {
        return count;
      }
      return searchCount(createSearchQuery());
    },
    { initialData: isDefaultQuery ? initialData.count : undefined }
  );

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

  const revalidate = useCallback(() => {
    queryCache.setQueryData(itemsCacheKey, undefined);
    queryCache.setQueryData(countCacheKey, undefined);

    refetchItems();
    refetchCount();
  }, [refetchCount, refetchItems, queryCache, itemsCacheKey, countCacheKey]);

  return {
    query,
    setQuery: onQueryChange,
    page,
    onPageChange,
    numPages,
    items,
    count,
    isSearching: isFetchingItems,
    isSearchingCount: isFetchingCount,
    revalidate,
  };
};
