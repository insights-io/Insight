import React from 'react';
import { CONSOLE_EVENTS, ERROR_EVENTS } from '__tests__/data';
import type { Meta } from '@storybook/react';

import { Console } from './Console';

export default {
  title: 'developer-tools/components/Console',
  component: Console,
} as Meta;

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
