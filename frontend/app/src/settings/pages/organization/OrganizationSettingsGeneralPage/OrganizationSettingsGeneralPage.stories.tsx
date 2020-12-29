import React from 'react';
import { configureStory, fullHeightDecorator } from '@rebrowse/storybook';
import { REBROWSE_ORGANIZATION_DTO } from '__tests__/data/organization';
import { REBROWSE_ADMIN_DTO } from '__tests__/data/user';
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
    const retrieveUserStub = sandbox.stub(AuthApi.user, 'me').resolves({
      data: { data: REBROWSE_ADMIN_DTO },
      statusCode: 200,
      headers: new Headers(),
    });

    const retrieveOrganizationStub = sandbox
      .stub(AuthApi.organization, 'get')
      .resolves({
        data: { data: REBROWSE_ORGANIZATION_DTO },
        statusCode: 200,
        headers: new Headers(),
      });

    return {
      retrieveUserStub,
      retrieveOrganizationStub,
    };
  },
});
