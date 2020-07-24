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

type Props = {
  session: Session;
};

const USER_AGENT_DEVICE_ICON_LOOKUP: Record<
  UserAgentDTO['browserName'],
  IconType
> = {
  Desktop: FaDesktop,
  Phone: FaMobileAlt,
};

const SessionListItem = ({
  session: { createdAt, location, userAgent },
}: Props) => {
  const [_css, theme] = useStyletron();

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
    <ListItem
      overrides={{
        Root: {
          style: {
            ':hover': { background: theme.colors.primary200 },
            borderRadius: theme.sizing.scale100,
          },
        },
      }}
      artwork={artwork}
      artworkSize={ARTWORK_SIZES.SMALL}
      endEnhancer={() => (
        <>
          <Tag
            closeable={false}
            overrides={{
              Root: {
                style: { ':hover': { cursor: 'pointer' } },
              },
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
  );
};

export default React.memo(SessionListItem);
