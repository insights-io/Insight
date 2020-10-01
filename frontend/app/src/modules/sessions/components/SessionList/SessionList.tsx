import React from 'react';
import InfiniteLoader from 'react-window-infinite-loader';
import AutoSizer from 'react-virtualized-auto-sizer';
import { FixedSizeList, FixedSizeListProps } from 'react-window';
import { Session } from '@insight/types';
import SessionListItem from 'modules/sessions/containers/SessionListItem';

type Props = {
  sessions: Session[];
  count: number;
  loadMoreItems: (startIndex: number, endIndex: number) => Promise<void>;
  isItemLoaded: (index: number) => boolean;
  overrides?: {
    List?: Partial<FixedSizeListProps>;
  };
};

export const SessionList = ({
  sessions,
  count,
  isItemLoaded,
  loadMoreItems,
  overrides,
}: Props) => {
  return (
    <InfiniteLoader
      isItemLoaded={isItemLoaded}
      itemCount={count}
      loadMoreItems={loadMoreItems}
    >
      {({ onItemsRendered, ref }) => (
        <AutoSizer>
          {({ height, width }) => (
            <FixedSizeList
              ref={ref}
              height={height}
              itemCount={count}
              itemSize={73}
              onItemsRendered={onItemsRendered}
              width={width}
              itemData={sessions}
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
