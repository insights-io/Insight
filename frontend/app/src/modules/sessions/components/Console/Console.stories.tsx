import React from 'react';
import { CONSOLE_EVENTS, ERROR_EVENTS } from 'test/data';

import Console from './Console';

export default {
  title: 'sessions/components/Console',
};

export const Base = () => {
  return (
    <Console
      events={[
        ...Object.values(CONSOLE_EVENTS),
        ...Object.values(ERROR_EVENTS),
      ]}
      loading={false}
    />
  );
};

export const Loading = () => {
  return <Console events={[]} loading />;
};
