import React from 'react';
import { ListItem } from 'baseui/list';
import { Skeleton } from 'baseui/skeleton';
import type { StyleObject } from 'styletron-react';

type Props = {
  style: StyleObject;
};

export const SessionListItemSkeleton = ({ style }: Props) => {
  return (
    <ListItem
      overrides={{
        Root: { style },
        Content: { style: { paddingRight: 0, marginLeft: 0 } },
      }}
    >
      <Skeleton width="100%" height="100%" animation />
    </ListItem>
  );
};
