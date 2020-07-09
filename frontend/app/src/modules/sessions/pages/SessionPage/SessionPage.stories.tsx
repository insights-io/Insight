import React from 'react';
import { INSIGHT_SESSION, INSIGHT_SESSION_DTO } from 'test/data';
import { configureStory, fullHeightDecorator } from '@insight/storybook';
import { SessionApi } from 'api';

import SessionPage from './SessionPage';

export default {
  title: 'sessions|pages/SessionPage',
  decorators: [fullHeightDecorator],
};

export const Base = () => {
  return <SessionPage sessionId={INSIGHT_SESSION.id} />;
};
Base.story = configureStory({
  setupMocks: (sandbox) => {
    return {
      getSessions: sandbox.stub(SessionApi, 'getSessions').resolves({
        data: [INSIGHT_SESSION_DTO],
      }),
      getEvents: sandbox.stub(SessionApi.events, 'get').resolves([
        {
          e: 9,
          level: 'log',
          arguments: ['[Fast Refresh] done'],
          t: 999,
        },
        {
          e: 9,
          level: 'warn',
          arguments: [
            `Vendors~main.6e71f8501d51c505cf1d.bundle.js:70044 The default hierarchy separators are changing in Storybook 6.0.
          '|' and '.' will no longer create a hierarchy, but codemods are available.
          Read more about it in the migration guide: https://github.com/storybookjs/storybook/blob/master/MIGRATION.md`,
          ],
          t: 1001,
        },
      ]),
    };
  },
});
