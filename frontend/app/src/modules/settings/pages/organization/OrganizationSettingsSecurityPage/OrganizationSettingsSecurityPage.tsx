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
  const { organization } = useOrganization(initialOrganization);

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
            explanation="Require and enforce two-factor authentication for all members"
            width="50%"
          >
            Require Two-Factor Authentication
          </ExplainedLabel>

          <VerticalAligned width="50%">
            <Block width="fit-content">
              <Toggle
                id="enforce2fa"
                name="enforce2fa"
                checked={false}
                disabled={false}
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
