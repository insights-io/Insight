import React from 'react';
import InfiniteLoader from 'react-window-infinite-loader';
import AutoSizer from 'react-virtualized-auto-sizer';
import { FixedSizeList } from 'react-window';
import { Session } from '@insight/types';
import SessionListItem from 'modules/sessions/containers/SessionListItem';
import useSessions from 'modules/sessions/hooks/useSessions';

type Props = {
  initialSessions: Session[];
  sessionCount: number;
};

const SessionList = ({ initialSessions, sessionCount }: Props) => {
  const { data: sessions, loadMoreItems, isItemLoaded } = useSessions(
    initialSessions
  );

  return (
    <InfiniteLoader
      isItemLoaded={isItemLoaded}
      itemCount={sessionCount}
      loadMoreItems={loadMoreItems}
    >
      {({ onItemsRendered, ref }) => (
        <AutoSizer>
          {({ height, width }) => (
            <FixedSizeList
              height={height}
              itemCount={sessionCount}
              itemSize={73}
              onItemsRendered={onItemsRendered}
              ref={ref}
              width={width}
              itemData={sessions}
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
