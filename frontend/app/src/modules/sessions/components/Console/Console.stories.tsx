import React from 'react';
import { CONSOLE_EVENTS, ERROR_EVENTS } from 'test/data';

import Console from './Console';

export default {
  title: 'sessions|components/Console',
};

export const Base = () => {
  return (
    <Console
      events={[
        CONSOLE_EVENTS.FAST_REFRESH_LOG,
        CONSOLE_EVENTS.STORYBOOK_WARN,
        CONSOLE_EVENTS.ERROR_LOG,
        CONSOLE_EVENTS.DEBUG_LOG,
        ERROR_EVENTS.ERROR,
        ERROR_EVENTS.SYNTAX_ERROR,
      ]}
      loading={false}
    />
  );
};

export const Loading = () => {
  return <Console events={[]} loading />;
};
