import React, { useState, useMemo } from 'react';
import { useStyletron } from 'baseui';
import Flex from 'shared/components/Flex';
import { Block } from 'baseui/block';
import { Input } from 'baseui/input';
import { StyleObject } from 'styletron-react';
import VerticalAligned from 'shared/components/VerticalAligned';
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

const Console = ({ events, loading, style }: Props) => {
  const [css, theme] = useStyletron();
  const [filterText, setFilterText] = useState('');

  const filteredEvents = useMemo(() => {
    return events.filter((e) => eventMatchesText(e, filterText));
  }, [events, filterText]);

  const renderEvent = (event: ConsoleEventDTO): React.ReactNode => {
    if (isErrorEvent(event)) {
      return <ConsoleErrorEvent event={event} />;
    }
    return <span>{event.arguments.join(' ')}</span>;
  };

  return (
    <section
      className={css({
        ...style,
        backgroundColor: '#d3d3d3',
        height: '100%',
        fontSize: '0.8rem',
        overflow: 'auto',
        display: 'flex',
        flexDirection: 'column',
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
            <StyledSpinnerNext />
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

export default Console;
