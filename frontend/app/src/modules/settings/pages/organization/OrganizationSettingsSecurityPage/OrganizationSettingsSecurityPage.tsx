import React from 'react';
import {
  SETTINGS_PATH_PART,
  ORGANIZATION_SETTINGS_PAGE_PART,
  ORGANIZATION_SETTINGS_SECURITY_PAGE_PART,
} from 'shared/constants/routes';
import { OrganizationSettingsPageLayout } from 'modules/settings/components/organization/OrganizationSettingsPageLayout';
import type { Path } from 'modules/settings/types';
import {
  OrganizationDTO,
  OrganizationPasswordPolicyDTO,
  UserDTO,
} from '@rebrowse/types';
import { useUser } from 'shared/hooks/useUser';
import { useOrganization } from 'shared/hooks/useOrganization';
import { FlexColumn, Panel, Toggle, VerticalAligned } from '@rebrowse/elements';
import { Block } from 'baseui/block';
import { useUpdateField } from 'shared/hooks/useUpdateField';
import { useStyletron } from 'baseui';

import { PasswordPolicyForm } from './PasswordPolicyForm';

const PATH: Path = [
  SETTINGS_PATH_PART,
  ORGANIZATION_SETTINGS_PAGE_PART,
  ORGANIZATION_SETTINGS_SECURITY_PAGE_PART,
];

type Props = {
  user: UserDTO;
  organization: OrganizationDTO;
  passwordPolicy: OrganizationPasswordPolicyDTO | undefined;
};

export const OrganizationSettingsSecurityPage = ({
  user: initialUser,
  organization: initialOrganization,
  passwordPolicy: initialPasswordPolicy,
}: Props) => {
  const [_css, theme] = useStyletron();
  const { user } = useUser(initialUser);
  const { organization, update } = useOrganization(initialOrganization);

  const {
    value: enforceMfa,
    updating: updatingEnforceMfa,
    updateNext: update2fa,
  } = useUpdateField({
    fieldName: 'enforceMultiFactorAuthentication',
    currentValue: organization.enforceMultiFactorAuthentication,
    resource: 'organization',
    update,
  });

  return (
    <OrganizationSettingsPageLayout
      path={PATH}
      user={user}
      organization={organization}
      header="Security"
    >
      <Panel>
        <Panel.Header>Security</Panel.Header>
        <Panel.Item display="flex" justifyContent="space-between">
          <Panel.Label
            for="enforce2fa"
            explanation="Require and enforce multi-factor authentication for all members"
            width="50%"
          >
            Require Multi-Factor Authentication
          </Panel.Label>

          <VerticalAligned width="50%">
            <Block width="fit-content">
              <Toggle
                id="enforceMfa"
                name="enforceMfa"
                checked={enforceMfa}
                disabled={updatingEnforceMfa}
                onChange={() => update2fa(!enforceMfa)}
              />
            </Block>
          </VerticalAligned>
        </Panel.Item>

        <Panel.Item
          display="flex"
          justifyContent="space-between"
          $style={{
            [`@media screen and (max-width: ${theme.breakpoints.medium}px)`]: {
              flexDirection: 'column',
            },
          }}
        >
          <Panel.Label
            explanation="Password policy is a set of rules that define complexity requirements for your organization members"
            width="50%"
            $style={{
              [`@media screen and (max-width: ${theme.breakpoints.medium}px)`]: {
                width: '100%',
              },
            }}
          >
            Set password policy
          </Panel.Label>

          <FlexColumn
            width="50%"
            $style={{
              [`@media screen and (max-width: ${theme.breakpoints.medium}px)`]: {
                width: '100%',
                marginTop: theme.sizing.scale800,
              },
            }}
          >
            <Block width="fit-content">
              <PasswordPolicyForm
                initialPasswordPolicy={initialPasswordPolicy}
              />
            </Block>
          </FlexColumn>
        </Panel.Item>
      </Panel>
    </OrganizationSettingsPageLayout>
  );
};
