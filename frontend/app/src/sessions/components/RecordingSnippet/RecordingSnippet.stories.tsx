import React from 'react';
import type { Meta } from '@storybook/react';

import { RecordingSnippet, Props } from './RecordingSnippet';

export default {
  title: 'setup/components/RecordingSnippet',
  component: RecordingSnippet,
} as Meta;

export const Base = (props?: Partial<Props>) => {
  return (
    <RecordingSnippet
      snippetUri="https://static.rebrowse.dev/b/rebrowse.js"
      organizationId="FE2Dj3"
      {...props}
    />
  );
};
