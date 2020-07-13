import React, { useState } from 'react';
import { BrowserErrorEventDTO } from '@insight/types';
import { Block } from 'baseui/block';
import VerticalAligned from 'shared/components/VerticalAligned';
import { ArrowDown, ArrowUp } from 'baseui/icon';
import { useStyletron } from 'baseui';
import { Accordion, Panel } from 'baseui/accordion';

type Props = {
  event: BrowserErrorEventDTO;
};

const ConsoleErrorEvent = ({ event }: Props) => {
  const [css, theme] = useStyletron();
  const loc = event.stack.split('\n')[1].trim();
  const [expanded, setExpanded] = useState(false);

  const ToggleComponent = expanded ? ArrowUp : ArrowDown;

  const baseStyling = {
    borderBottomWidth: 0,
    backgroundColor: 'inherit',
    fontSize: 'inherit',
  } as const;

  return (
    <Accordion onChange={(data) => setExpanded(data.expanded.length === 1)}>
      <Panel
        overrides={{
          Header: {
            style: {
              paddingTop: 0,
              paddingLeft: 0,
              paddingBottom: 0,
              paddingRight: 0,
              ...baseStyling,
            },
          },
          ToggleIcon: { style: { display: 'none' } },
          Content: {
            style: {
              ...baseStyling,
              paddingLeft: theme.sizing.scale300,
              ...(expanded
                ? {
                    paddingTop: theme.sizing.scale300,
                    paddingBottom: theme.sizing.scale300,
                  }
                : {}),
            },
          },
        }}
        title={
          <Block display="flex" justifyContent="space-between" width="100%">
            <Block display="flex">
              <VerticalAligned>
                <ToggleComponent size={16} color={theme.colors.mono500} />
              </VerticalAligned>
              <span className={css({ color: theme.colors.negative400 })}>
                Uncaught {event.name}: {event.message}
              </span>
            </Block>
            <span className={css({ color: theme.colors.negative400 })}>
              {loc}
            </span>
          </Block>
        }
      >
        <span className={css({ color: theme.colors.white })}>
          <pre className={css({ margin: 0 })}>
            <code>{event.stack}</code>
          </pre>
        </span>
      </Panel>
    </Accordion>
  );
};

export default React.memo(ConsoleErrorEvent);
