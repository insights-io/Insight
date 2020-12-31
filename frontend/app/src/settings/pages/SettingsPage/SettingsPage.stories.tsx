import React from 'react';
import { configureStory, fullHeightDecorator } from '@rebrowse/storybook';
import type { Meta } from '@storybook/react';
import { REBROWSE_ADMIN_DTO, REBROWSE_ORGANIZATION_DTO } from '__tests__/data';
import { AuthApi } from 'api';
import { httpOkResponse } from '__tests__/utils/request';

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
    const retrieveUserStub = sandbox
      .stub(AuthApi.user, 'me')
      .resolves(httpOkResponse(REBROWSE_ADMIN_DTO));

    const retrieveOrganizationStub = sandbox
      .stub(AuthApi.organization, 'get')
      .resolves(httpOkResponse(REBROWSE_ORGANIZATION_DTO));

    return { retrieveUserStub, retrieveOrganizationStub };
  },
});
