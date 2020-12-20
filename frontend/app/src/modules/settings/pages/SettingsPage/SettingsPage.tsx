import { AppLayout } from 'modules/app/components/AppLayout';
import React from 'react';
import {
  ACCOUNT_SETTINGS_DETAILS_PAGE,
  ACCOUNT_SETTINGS_SECURITY_PAGE,
  ORGANIZATION_SETTINGS_AUTH_PAGE,
  ORGANIZATION_SETTINGS_GENERAL_PAGE,
  ORGANIZATION_SETTINGS_MEMBERS_PAGE,
  SETTINGS_PATH_PART,
} from 'shared/constants/routes';
import { SettingsLayout } from 'modules/settings/components/SettingsLayout';
import { SETTINGS_SEARCH_OPTIONS } from 'modules/settings/constants';
import { Flex } from '@rebrowse/elements';
import { OrganizationDTO, UserDTO } from '@rebrowse/types';
import { useUser } from 'shared/hooks/useUser';
import { useOrganization } from 'shared/hooks/useOrganization';
import { MEMBERS_SECTION } from 'shared/constants/copy';
import type { Path } from 'modules/settings/types';
import { useStyletron } from 'baseui';

import { SettingsSectionCard } from './SettingsSectionCard';

const PATH: Path = [SETTINGS_PATH_PART];

type Props = {
  user: UserDTO;
  organization: OrganizationDTO;
};

export const SettingsPage = ({
  user: initialUser,
  organization: initialOrganization,
}: Props) => {
  const { user } = useUser(initialUser);
  const { organization } = useOrganization(initialOrganization);
  const [_css, theme] = useStyletron();

  return (
    <AppLayout user={user} organization={organization}>
      <SettingsLayout searchOptions={SETTINGS_SEARCH_OPTIONS} path={PATH}>
        <Flex padding={theme.sizing.scale200} flexWrap>
          <SettingsSectionCard
            header="My account"
            headerLink={ACCOUNT_SETTINGS_DETAILS_PAGE}
            avatar={user.fullName || user.email}
            quickLinks={[
              {
                text: 'Change my password',
                link: ACCOUNT_SETTINGS_SECURITY_PAGE,
              },
              {
                text: 'Setup Multi-Factor Authentication',
                link: ACCOUNT_SETTINGS_SECURITY_PAGE,
              },
            ]}
          />

          <SettingsSectionCard
            header={organization.name || 'Organization'}
            headerLink={ORGANIZATION_SETTINGS_GENERAL_PAGE}
            avatar={organization.name || 'O'}
            quickLinks={[
              {
                text: MEMBERS_SECTION,
                link: ORGANIZATION_SETTINGS_MEMBERS_PAGE,
              },
              {
                text: 'Setup authentication',
                link: ORGANIZATION_SETTINGS_AUTH_PAGE,
              },
            ]}
          />
        </Flex>
      </SettingsLayout>
    </AppLayout>
  );
};
