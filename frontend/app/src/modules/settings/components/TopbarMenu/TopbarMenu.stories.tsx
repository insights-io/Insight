import React from 'react';
import { fullHeightDecorator } from '@insight/storybook';
import { SETTINGS_SEARCH_OPTIONS } from 'modules/settings/constants';
import type { Meta } from '@storybook/react';
import { action } from '@storybook/addon-actions';

import { TopbarMenu } from './TopbarMenu';

export default {
  title: 'settings/components/TopbarMenu',
  component: TopbarMenu,
  decorators: [fullHeightDecorator],
} as Meta;

export const SettingsSearchOptions = () => {
  return (
    <TopbarMenu
      isSidebarOverlay={false}
      overlaySidebarOpen={false}
      setOverlaySidebarOpen={action('setOverlaySidebarOpen')}
      path={[
        { segment: 'settings', text: 'Settings' },
        { segment: 'organization', text: 'Organization' },
        { segment: 'security-and-privacy', text: 'Security & Privacy' },
      ]}
      searchOptions={SETTINGS_SEARCH_OPTIONS}
    />
  );
};
