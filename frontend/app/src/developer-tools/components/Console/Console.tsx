/* eslint-disable lodash/prefer-lodash-typecheck */
import React, { useState, useMemo } from 'react';
import { useStyletron } from 'baseui';
import { Block } from 'baseui/block';
import { Input } from 'baseui/input';
import { StyleObject } from 'styletron-react';
import { Flex, VerticalAligned } from '@rebrowse/elements';
import Divider from 'shared/components/Divider';
import { StyledSpinnerNext } from 'baseui/spinner';

import {
  ConsoleEventDTO,
  isErrorEvent,
  getEventStyling,
  getEventIcon,
  eventMatchesText,
} from './utils';
import ConsoleErrorEvent from './ConsoleErrorEvent';

type Props = {
  events: ConsoleEventDTO[];
  loading: boolean;
  style?: StyleObject;
};

export const Console = ({ events, loading, style }: Props) => {
  const [css, theme] = useStyletron();
  const [filterText, setFilterText] = useState('');

  const filteredEvents = useMemo(() => {
    return events.filter((e) => eventMatchesText(e, filterText));
  }, [events, filterText]);

  const renderEvent = (event: ConsoleEventDTO): React.ReactNode => {
    if (isErrorEvent(event)) {
      return <ConsoleErrorEvent event={event} />;
    }
    return (
      <span>
        {event.arguments
          // TODO: smarter rendering of objects
          .map((v) => (typeof v === 'object' ? JSON.stringify(v) : v))
          .join(' ')}
      </span>
    );
  };

  return (
    <section
      className={css({
        ...style,
        background: '#d3d3d3',
        height: '100%',
        fontSize: '0.8rem',
        overflow: 'auto',
        display: 'flex',
        flexDirection: 'column',
        width: '100%',
      })}
    >
      <Block
        backgroundColor="#d3d3d3"
        padding={theme.sizing.scale300}
        display="flex"
      >
        <Input
          size="mini"
          placeholder="Filter"
          clearable
          overrides={{ Root: { style: { maxWidth: '120px' } } }}
          value={filterText}
          onChange={(event) => setFilterText(event.currentTarget.value)}
        />
      </Block>

      <Divider marginTop={0} marginBottom={0} />

      <Block height="100%" overflow="auto">
        {loading ? (
          <Block display="flex" justifyContent="center">
            <StyledSpinnerNext $style={{ marginTop: theme.sizing.scale500 }} />
          </Block>
        ) : (
          filteredEvents.map((event) => {
            const { backgroundColor, color } = getEventStyling(event);
            const IconComponent = getEventIcon(event);

            return (
              <Flex
                key={JSON.stringify(event)}
                $style={{
                  backgroundColor,
                  padding: theme.sizing.scale100,
                }}
              >
                {IconComponent && (
                  <VerticalAligned $style={{ maxHeight: '20px' }}>
                    <IconComponent color={color} />
                  </VerticalAligned>
                )}
                <Block
                  width="100%"
                  marginLeft={theme.sizing.scale300}
                  marginRight={theme.sizing.scale300}
                  color={color}
                  $style={{ wordBreak: 'break-word' }}
                >
                  {renderEvent(event)}
                </Block>
              </Flex>
            );
          })
        )}
      </Block>
    </section>
  );
};
