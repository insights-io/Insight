import React from 'react';
import { Session, UserAgentDTO } from '@insight/types';
import { ListItem, ARTWORK_SIZES, ListItemLabel } from 'baseui/list';
import { Tag } from 'baseui/tag';
import UserAgent from 'shared/components/UserAgent';
import { FaDesktop, FaMobileAlt } from 'react-icons/fa';
import { useStyletron } from 'baseui';
import { formatDistanceToNow } from 'date-fns';
import { readableLocation } from 'shared/utils/location';
import { ChevronRight } from 'baseui/icon';
import { IconType } from 'react-icons/lib';
import { ListChildComponentProps } from 'react-window';
import Link from 'next/link';
import SesssionListItemSkeleton from 'modules/sessions/components/SessionListItemSkeleton';

type Props = ListChildComponentProps;

const USER_AGENT_DEVICE_ICON_LOOKUP: Record<
  UserAgentDTO['browserName'],
  IconType
> = {
  Desktop: FaDesktop,
  Phone: FaMobileAlt,
};

const SessionListItem = ({ data, index, style }: Props) => {
  const [css, theme] = useStyletron();
  const sessions = data as Session[];

  const listItemStyle = {
    borderRadius: theme.sizing.scale100,
    ...style,
    top: `${style.top}px`,
  };

  if (index >= sessions.length) {
    return <SesssionListItemSkeleton style={listItemStyle} />;
  }

  const { id, createdAt, location, userAgent } = sessions[index];

  const createdAtText = formatDistanceToNow(createdAt, {
    includeSeconds: true,
    addSuffix: true,
  });

  const description = [
    readableLocation(location),
    location.ip,
    createdAtText,
  ].join(' - ');

  const artwork =
    USER_AGENT_DEVICE_ICON_LOOKUP[userAgent.deviceClass] || FaDesktop;

  return (
    <Link href="/sessions/[id]" as={`sessions/${id}`} key={id}>
      <a className={css({ color: 'inherit' })}>
        <ListItem
          artwork={artwork}
          artworkSize={ARTWORK_SIZES.SMALL}
          overrides={{
            Root: {
              style: {
                ...listItemStyle,
                ':hover': { background: theme.colors.primary200 },
              },
            },
          }}
          endEnhancer={() => (
            <>
              <Tag
                closeable={false}
                overrides={{
                  Root: { style: { ':hover': { cursor: 'pointer' } } },
                }}
              >
                Details
              </Tag>
              <ChevronRight />
            </>
          )}
        >
          <ListItemLabel description={description}>
            <UserAgent value={userAgent} />
          </ListItemLabel>
        </ListItem>
      </a>
    </Link>
  );
};

export default React.memo(SessionListItem);
