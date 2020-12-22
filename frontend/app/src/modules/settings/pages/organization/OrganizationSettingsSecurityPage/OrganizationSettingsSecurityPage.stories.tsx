import React from 'react';
import { configureStory, fullHeightDecorator } from '@rebrowse/storybook';
import type { Meta } from '@storybook/react';
import { REBROWSE_ORGANIZATION_DTO, REBROWSE_ADMIN_DTO } from '__tests__/data';
import { AuthApi } from 'api';

import { OrganizationSettingsSecurityPage } from './OrganizationSettingsSecurityPage';

export default {
  title: 'settings/pages/organization/OrganizationSettingsSecurityPage',
  component: OrganizationSettingsSecurityPage,
  decorators: [fullHeightDecorator],
} as Meta;

export const Base = () => {
  return (
    <OrganizationSettingsSecurityPage
      passwordPolicy={undefined}
      user={REBROWSE_ADMIN_DTO}
      organization={REBROWSE_ORGANIZATION_DTO}
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

    const retrievePasswordPolicyStub = sandbox
      .stub(AuthApi.organization.passwordPolicy, 'retrieve')
      .resolves(undefined);

    return {
      retrieveUserStub,
      retrieveOrganizationStub,
      retrievePasswordPolicyStub,
    };
  },
});
