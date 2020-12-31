import { SessionApi } from 'api/session';
import {
  useMemo,
  useCallback,
  useEffect,
  useRef,
  useReducer,
  Reducer,
} from 'react';
import { Session, SessionDTO } from '@rebrowse/types';
import { mapSession, TermCondition } from '@rebrowse/sdk';
import debounce from 'lodash/debounce';
import { UnreachableCaseError } from 'shared/utils/error';
import { SessionSearchBean } from '@rebrowse/sdk/dist/sessions';
import { DateRange } from 'sessions/components/SessionSearch/utils';
import { SessionFilter } from 'sessions/components/SessionSearch/SessionFilters/utils';

const EMPTY_FILTER: Filter = { filters: [] };

type Filter = {
  dateRange?: DateRange;
  filters: SessionFilter[];
};

type State = {
  data: Session[];
  count: number;
  fetchingStartIndex: number | undefined;
};

const actionTypes = {
  SET_SESSIONS: 'SET_SESSIONS',
  SET_FETCHING_START_INDEX: 'SET_FETCHING_START_INDEX',
  ADD_SESSIONS: 'ADD_SESSIONS',
  SET_COUNT: 'SET_COUNT',
} as const;

type StateAction =
  | {
      type: typeof actionTypes.SET_SESSIONS;
      sessions: Session[];
    }
  | {
      type: typeof actionTypes.ADD_SESSIONS;
      sessions: Session[];
    }
  | {
      type: typeof actionTypes.SET_COUNT;
      sessions: Session[];
      count: number;
    }
  | { type: typeof actionTypes.SET_FETCHING_START_INDEX; index: number };

const stateReducer: Reducer<State, StateAction> = (state, action) => {
  switch (action.type) {
    case actionTypes.SET_SESSIONS:
      return {
        ...state,
        count: action.sessions.length,
        data: action.sessions,
        fetchingStartIndex: undefined,
      };
    case actionTypes.SET_COUNT:
      return { ...state, count: action.count, data: action.sessions };
    case actionTypes.ADD_SESSIONS: {
      const data = [...state.data, ...action.sessions];
      return {
        ...state,
        data,
        fetchingStartIndex: undefined,
      };
    }
    case actionTypes.SET_FETCHING_START_INDEX:
      return { ...state, fetchingStartIndex: action.index };
    default:
      throw new UnreachableCaseError(action);
  }
};

const getSearchQuery = ({ dateRange, filters }: Filter) => {
  const searchBean: SessionSearchBean<[]> = {};
  const createdAt = [];
  if (dateRange?.from) {
    createdAt.push(TermCondition.GTE(dateRange.from));
  }
  if (dateRange?.to) {
    createdAt.push(TermCondition.LTE(dateRange.to));
  }

  if (createdAt.length > 0) {
    searchBean.createdAt = createdAt;
  }

  filters.forEach((f) => {
    if (f.key) {
      searchBean[f.key] = TermCondition.EQ(f.value);
    }
  });

  return searchBean;
};

export const useSessions = (
  initialSessions: SessionDTO[],
  initialSessionCount: number,
  filter: Filter = EMPTY_FILTER
) => {
  const isMounted = useRef(false);
  const [{ data, fetchingStartIndex, count }, dispatch] = useReducer(
    stateReducer,
    undefined,
    () => {
      return {
        fetchingStartIndex: undefined,
        data: initialSessions.map(mapSession),
        count: initialSessionCount,
      };
    }
  );

  const sessions = useMemo(() => data, [data]);

  const onFilterChange = useMemo(
    () =>
      debounce(async (paramFilter: Filter) => {
        dispatch({ type: actionTypes.SET_SESSIONS, sessions: [] });

        const search = getSearchQuery(paramFilter);
        const countPromise = SessionApi.count({
          search: getSearchQuery(paramFilter),
        }).then((httpResponse) => httpResponse.data.count);

        search.limit = 20;
        search.sortBy = ['-createdAt'];

        const sessionsPromise = SessionApi.getSessions({
          search,
        }).then((httpResponse) => httpResponse.data.map(mapSession));

        Promise.all([countPromise, sessionsPromise]).then(
          ([nextCount, nextSessions]) => {
            dispatch({
              type: actionTypes.SET_COUNT,
              count: nextCount,
              sessions: nextSessions,
            });
          }
        );
      }, 500),
    []
  );

  useEffect(() => {
    if (isMounted.current) {
      onFilterChange(filter);
    } else {
      isMounted.current = true;
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [JSON.stringify(filter), onFilterChange]);

  const loadMoreItems = useCallback(
    async (startIndex: number, endIndex: number) => {
      if (sessions.length !== startIndex || startIndex === fetchingStartIndex) {
        return;
      }

      dispatch({
        type: actionTypes.SET_FETCHING_START_INDEX,
        index: startIndex,
      });

      const search = getSearchQuery(filter);
      search.sortBy = ['-createdAt'];
      search.limit = endIndex - startIndex + 1;

      if (sessions.length > 0) {
        const lastSessionCreatedAt = TermCondition.LTE(
          sessions[sessions.length - 1].createdAt
        );

        if (!search.createdAt) {
          search.createdAt = [lastSessionCreatedAt];
        } else {
          const createdAtFilter = search.createdAt as string[];
          const createdAtLteIndex = createdAtFilter.findIndex((v) =>
            v.startsWith('lte:')
          );
          if (createdAtLteIndex === -1) {
            createdAtFilter.push(lastSessionCreatedAt);
          } else {
            createdAtFilter[createdAtLteIndex] = lastSessionCreatedAt;
          }
        }
      }

      SessionApi.getSessions({ search })
        .then((httpResponse) => httpResponse.data.map(mapSession))
        .then((newSessions) =>
          dispatch({
            type: actionTypes.ADD_SESSIONS,
            sessions: newSessions,
          })
        );
    },
    [sessions, fetchingStartIndex, filter]
  );

  const isItemLoaded = useCallback(
    (index: number) => {
      return index < sessions.length;
    },
    [sessions]
  );

  const loading = useMemo(() => data === undefined, [data]);

  return {
    sessions,
    loading,
    count,
    loadMoreItems,
    isItemLoaded,
    isLoadingMore: fetchingStartIndex !== undefined,
  };
};
