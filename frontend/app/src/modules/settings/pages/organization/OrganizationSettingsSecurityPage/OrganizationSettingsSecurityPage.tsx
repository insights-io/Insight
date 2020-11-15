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
} from '@insight/types';
import { useUser } from 'shared/hooks/useUser';
import { useOrganization } from 'shared/hooks/useOrganization';
import {
  ExplainedLabel,
  FlexColumn,
  Panel,
  Toggle,
  VerticalAligned,
} from '@insight/elements';
import { Block } from 'baseui/block';
import { useUpdateField } from 'shared/hooks/useUpdateField';

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
  const { user } = useUser(initialUser);
  const { organization, updateOrganization } = useOrganization(
    initialOrganization
  );

  const {
    value: enforceMfa,
    updating: updatingEnforceMfa,
    updateNext: update2fa,
  } = useUpdateField({
    fieldName: 'enforceMultiFactorAuthentication',
    currentValue: organization.enforceMultiFactorAuthentication,
    resource: 'organization',
    update: updateOrganization,
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
          <ExplainedLabel
            for="enforce2fa"
            explanation="Require and enforce multi-factor authentication for all members"
            width="50%"
          >
            Require Multi-Factor Authentication
          </ExplainedLabel>

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

        <Panel.Item display="flex" justifyContent="space-between">
          <ExplainedLabel
            explanation="Password policy is a set of rules that define complexity requirements for your organization members"
            width="50%"
          >
            Set password policy
          </ExplainedLabel>

          <FlexColumn width="50%">
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
