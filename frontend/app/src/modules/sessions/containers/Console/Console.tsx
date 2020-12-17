import React from 'react';
import Console, { ConsoleEventDTO } from 'modules/sessions/components/Console';

import { useEvents } from './useEvents';

type Props = {
  sessionId: string;
};

const ConsoleContainer = ({ sessionId }: Props) => {
  const { data } = useEvents(sessionId);

  const loading = data === undefined;
  const events = (data || []) as ConsoleEventDTO[];

  return <Console events={events.slice(0, 50)} loading={loading} />;
};

export default React.memo(ConsoleContainer);
