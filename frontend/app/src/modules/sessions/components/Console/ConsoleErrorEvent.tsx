import React from 'react';
import { BrowserErrorEventDTO } from '@insight/types';
import { Block } from 'baseui/block';
import VerticalAligned from 'shared/components/VerticalAligned';
import { ArrowDown } from 'baseui/icon';
import { useStyletron } from 'baseui';

type Props = {
  event: BrowserErrorEventDTO;
};

const ConsoleErrorEvent = ({ event }: Props) => {
  const [_css, theme] = useStyletron();
  const loc = event.stack.split('\n')[1].trim();

  return (
    <Block display="flex" justifyContent="space-between">
      <Block display="flex">
        <VerticalAligned>
          <ArrowDown size={16} color={theme.colors.mono700} />
        </VerticalAligned>
        <span>
          Uncaught {event.name}: {event.message}
        </span>
      </Block>
      <span>{loc}</span>
    </Block>
  );
};

export default React.memo(ConsoleErrorEvent);
