import React from 'react';
import { configureStory, fullHeightDecorator } from '@rebrowse/storybook';
import type { Meta } from '@storybook/react';
import { REBROWSE_ADMIN_DTO, REBROWSE_ORGANIZATION_DTO } from '__tests__/data';
import { AuthApi } from 'api';

import { SettingsPage } from './SettingsPage';

export default {
  title: 'settings/pages/SettingsPage',
  component: SettingsPage,
  decorators: [fullHeightDecorator],
} as Meta;

export const Base = () => {
  return (
    <SettingsPage
      user={REBROWSE_ADMIN_DTO}
      organization={REBROWSE_ORGANIZATION_DTO}
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

    return { retrieveUserStub, retrieveOrganizationStub };
  },
});
