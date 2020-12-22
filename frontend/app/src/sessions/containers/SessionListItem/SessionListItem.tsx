import React from 'react';
import { Session } from '@rebrowse/types';
import { useStyletron } from 'baseui';
import type { ListChildComponentProps } from 'react-window';
import { SessionListItemSkeleton } from 'sessions/components/SessionListItemSkeleton';
import { SessionListItem } from 'sessions/components/SessionListItem';

type Props = ListChildComponentProps;

export const SessionListItemContainer = React.memo(
  ({ data, index, style }: Props) => {
    const [_css, theme] = useStyletron();
    const sessions = data as Session[];

    const listItemStyle = {
      ...style,
      borderRadius: theme.sizing.scale100,
      top: `${style.top}px`,
    };

    if (index >= sessions.length) {
      return <SessionListItemSkeleton style={listItemStyle} />;
    }

    return <SessionListItem session={sessions[index]} style={style} />;
  }
);
