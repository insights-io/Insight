import React from 'react';
import { EventType } from '@insight/types';

import Console from './Console';

export default {
  title: 'Sessions|Console',
};

export const Base = () => {
  return (
    <Console
      events={[
        {
          e: '9' as EventType.LOG,
          level: 'log',
          arguments: ['[Fast Refresh] done'],
          t: 999,
        },
        {
          e: '9' as EventType.LOG,
          level: 'warn',
          arguments: [
            `Vendors~main.6e71f8501d51c505cf1d.bundle.js:70044 The default hierarchy separators are changing in Storybook 6.0.
          '|' and '.' will no longer create a hierarchy, but codemods are available.
          Read more about it in the migration guide: https://github.com/storybookjs/storybook/blob/master/MIGRATION.md`,
          ],
          t: 1001,
        },
        {
          e: '9' as EventType.LOG,
          level: 'error',
          arguments: ['HAHA'],
          t: 1001,
        },
        {
          e: '9' as EventType.LOG,
          level: 'debug',
          arguments: ['HAHA'],
          t: 1001,
        },
      ]}
    />
  );
};
