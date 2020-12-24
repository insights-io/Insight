import React from 'react';
import { Console, ConsoleEventDTO } from 'developer-tools/components/Console';

import { useEvents } from './useEvents';

export type ConsoleContainerProps = {
  sessionId: string;
};

export const ConsoleContainer = React.memo(
  ({ sessionId }: ConsoleContainerProps) => {
    const { data } = useEvents(sessionId);

    const loading = data === undefined;
    const events = (data || []) as ConsoleEventDTO[];

    return <Console events={events.slice(0, 50)} loading={loading} />;
  }
);

export default ConsoleContainer;
