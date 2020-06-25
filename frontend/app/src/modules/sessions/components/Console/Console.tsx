import React, { useState, useMemo } from 'react';
import { useStyletron } from 'baseui';
import { DeleteAlt, Alert, IconProps } from 'baseui/icon';
import Flex from 'shared/components/Flex';
import { Block } from 'baseui/block';
import { Input } from 'baseui/input';
import { StyleObject } from 'styletron-react';

export type LogEvent = {
  t: number;
  e: '9';
  level: 'log' | 'info' | 'debug' | 'error' | 'warn';
  arguments: string[];
};

type Props = {
  events: LogEvent[];
  style?: StyleObject;
};

const LEVEL_COLOR_MAPPINGS: Record<
  LogEvent['level'],
  { backgroundColor: string; color: string }
> = {
  warn: { backgroundColor: '#e65100', color: '#ffc107' },
  error: { backgroundColor: '#7f0000', color: '#DB4437' },
  debug: { backgroundColor: 'transparent', color: '#1e88e5' },
  log: { backgroundColor: 'transparent', color: '#000' },
  info: { backgroundColor: 'transparent', color: '#000' },
};

const LEVEL_ICON_MAPPINGS: Record<
  LogEvent['level'],
  React.FC<IconProps> | null
> = {
  error: DeleteAlt,
  warn: Alert,
  debug: null,
  log: null,
  info: null,
};

const Console = ({ events, style }: Props) => {
  const [css, theme] = useStyletron();
  const [filterText, setFilterText] = useState('');

  const filteredEvents = useMemo(() => {
    return events.filter((e) => e.arguments.join(' ').includes(filterText));
  }, [events, filterText]);

  return (
    <section
      className={css({
        ...style,
        backgroundColor: '#d3d3d3',
        height: '100%',
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
      <Block height="100%" overflow="scroll">
        {filteredEvents.map((event) => {
          const { backgroundColor, color } = LEVEL_COLOR_MAPPINGS[event.level];
          const IconComponent = LEVEL_ICON_MAPPINGS[event.level];

          return (
            <Flex
              key={JSON.stringify(event)}
              $style={{
                backgroundColor,
                padding: theme.sizing.scale100,
              }}
            >
              {IconComponent && (
                <Block>
                  <IconComponent color={color} />
                </Block>
              )}
              <Block
                $style={{
                  color,
                  wordBreak: 'break-word',
                  marginLeft: theme.sizing.scale300,
                  marginRight: theme.sizing.scale300,
                }}
              >
                {event.arguments.join(' ')}
              </Block>
            </Flex>
          );
        })}
      </Block>
    </section>
  );
};

export default Console;
