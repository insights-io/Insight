import React, { useMemo } from 'react';
import InfiniteLoader from 'react-window-infinite-loader';
import AutoSizer from 'react-virtualized-auto-sizer';
import { FixedSizeList, FixedSizeListProps } from 'react-window';
import { Session } from '@insight/types';
import SessionListItem from 'modules/sessions/containers/SessionListItem';
import useSessions from 'modules/sessions/hooks/useSessions';
import { DateRange } from 'modules/sessions/components/SessionSearch/utils';
import { SessionFilter } from 'modules/sessions/components/SessionSearch/SessionFilters/utils';

type Props = {
  initialSessions: Session[];
  initialSessionCount: number;
  dateRange: DateRange;
  filters: SessionFilter[];
  overrides?: {
    List?: Partial<FixedSizeListProps>;
  };
};

const SessionList = ({
  initialSessions,
  initialSessionCount,
  dateRange,
  filters,
  overrides,
}: Props) => {
  const options = useMemo(() => ({ dateRange, filters }), [dateRange, filters]);
  const { data, count, loadMoreItems, isItemLoaded } = useSessions(
    initialSessions,
    initialSessionCount,
    options
  );

  return (
    <InfiniteLoader
      isItemLoaded={isItemLoaded}
      itemCount={count}
      loadMoreItems={loadMoreItems}
    >
      {({ onItemsRendered, ref }) => (
        <AutoSizer ref={ref}>
          {({ height, width }) => (
            <FixedSizeList
              height={height}
              itemCount={count}
              itemSize={73}
              onItemsRendered={onItemsRendered}
              width={width}
              itemData={data}
              {...overrides?.List}
            >
              {SessionListItem}
            </FixedSizeList>
          )}
        </AutoSizer>
      )}
    </InfiniteLoader>
  );
};

export default SessionList;
