import React from 'react';
import type { Session } from '@rebrowse/types';
import { ListItem, ARTWORK_SIZES, ListItemLabel } from 'baseui/list';
import { Tag } from 'baseui/tag';
import UserAgent from 'shared/components/UserAgent';
import { useStyletron } from 'baseui';
import { ChevronRight } from 'baseui/icon';
import { ListChildComponentProps } from 'react-window';
import Link from 'next/link';
import { SESSIONS_PAGE } from 'shared/constants/routes';
import { sessionDescription } from 'sessions/utils';
import { getDeviceClassIcon } from 'sessions/utils/user-agent';

type Props = Pick<ListChildComponentProps, 'style'> & {
  session: Session;
};

export const SessionListItem = ({ session, style }: Props) => {
  const { id, userAgent } = session;
  const [css, theme] = useStyletron();
  const description = sessionDescription(session);
  const artwork = getDeviceClassIcon(session.userAgent.deviceClass);
  const link = `${SESSIONS_PAGE}/${id}`;

  return (
    <Link href={link} key={id}>
      <a className={css({ color: 'inherit' })}>
        <ListItem
          artwork={artwork}
          artworkSize={ARTWORK_SIZES.SMALL}
          overrides={{
            Root: {
              style: {
                ':hover': { background: theme.colors.primary200 },
                borderRadius: theme.sizing.scale100,
                ...style,
                top: `${style.top}px`,
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
