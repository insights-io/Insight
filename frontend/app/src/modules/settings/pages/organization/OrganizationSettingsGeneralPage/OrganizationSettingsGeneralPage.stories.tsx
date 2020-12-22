import React from 'react';
import { configureStory, fullHeightDecorator } from '@rebrowse/storybook';
import { REBROWSE_ORGANIZATION_DTO } from 'test/data/organization';
import { REBROWSE_ADMIN_DTO } from 'test/data';
import type { Meta } from '@storybook/react';
import { AuthApi } from 'api';

import { OrganizationSettingsGeneralPage } from './OrganizationSettingsGeneralPage';

export default {
  title: 'settings/pages/organization/OrganizationSettingsGeneralPage',
  component: OrganizationSettingsGeneralPage,
  decorators: [fullHeightDecorator],
} as Meta;

export const Base = () => {
  return (
    <OrganizationSettingsGeneralPage
      organization={REBROWSE_ORGANIZATION_DTO}
      user={REBROWSE_ADMIN_DTO}
    />
  );
};

Base.story = configureStory({
  setupMocks: (sandbox) => {
    const retrieveUserStub = sandbox
      .stub(AuthApi.user, 'me')
      .resolves(REBROWSE_ADMIN_DTO);

    const retrieveOrganizationStub = sandbox
      .stub(AuthApi.organization, 'get')
      .resolves(REBROWSE_ORGANIZATION_DTO);

    return {
      retrieveUserStub,
      retrieveOrganizationStub,
    };
  },
});
