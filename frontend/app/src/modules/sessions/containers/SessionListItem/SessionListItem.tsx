import React from 'react';
import { Session } from '@rebrowse/types';
import { useStyletron } from 'baseui';
import { ListChildComponentProps } from 'react-window';
import SesssionListItemSkeleton from 'modules/sessions/components/SessionListItemSkeleton';
import SessionListItem from 'modules/sessions/components/SessionListItem';

type Props = ListChildComponentProps;

const SessionListItemContainer = ({ data, index, style }: Props) => {
  const [_css, theme] = useStyletron();
  const sessions = data as Session[];

  const listItemStyle = {
    ...style,
    borderRadius: theme.sizing.scale100,
    top: `${style.top}px`,
  };

  if (index >= sessions.length) {
    return <SesssionListItemSkeleton style={listItemStyle} />;
  }

  return <SessionListItem session={sessions[index]} style={style} />;
};

export default React.memo(SessionListItemContainer);
