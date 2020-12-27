import React, { useState } from 'react';
import { fullHeightDecorator } from '@rebrowse/storybook';
import { SIDEBAR_SECTIONS } from 'settings/components/organization/OrganizationSettingsLayout';
import type { Meta } from '@storybook/react';

import { SidebarMenu } from './SidebarMenu';

export default {
  title: 'settings/components/SidebarMenu',
  component: SidebarMenu,
  decorators: [fullHeightDecorator],
} as Meta;

export const OrganizationSettingsSidebarSections = () => {
  const [pathname, setPathname] = useState(SIDEBAR_SECTIONS[0].items[0].link);
  return (
    <SidebarMenu
      pathname={pathname}
      onItemClick={setPathname}
      sections={SIDEBAR_SECTIONS}
    />
  );
};
