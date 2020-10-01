import React from 'react';
import { fullHeightDecorator } from '@insight/storybook';
import type { Meta } from '@storybook/react';
import { INSIGHT_ADMIN_DTO } from 'test/data';

import { OrganizationSettingsSecurityAndPrivacyPage } from './OrganizationSettingsSecurityAndPrivacyPage';

export default {
  title:
    'settings/pages/organization/OrganizationSettingsSecurityAndPrivacyPage',
  component: OrganizationSettingsSecurityAndPrivacyPage,
  decorators: [fullHeightDecorator],
} as Meta;

export const Base = () => {
  return (
    <OrganizationSettingsSecurityAndPrivacyPage user={INSIGHT_ADMIN_DTO} />
  );
};
