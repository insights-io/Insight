import React from 'react';
import {
  SETTINGS_PATH_PART,
  ORGANIZATION_SETTINGS_PAGE_PART,
  ORGANIZATION_SETTINGS_GENERAL_PAGE_PART,
} from 'shared/constants/routes';
import { OrganizationSettingsPageLayout } from 'modules/settings/components/organization/OrganizationSettingsPageLayout';
import { useOrganization } from 'shared/hooks/useOrganization';
import { useUser } from 'shared/hooks/useUser';
import {
  Panel,
  VerticalAligned,
  Input,
  SpacedBetween,
  AutocompleteInput,
  Toggle,
} from '@rebrowse/elements';
import { useUpdateField } from 'shared/hooks/useUpdateField';
import type { Path } from 'modules/settings/types';
import type { OrganizationDTO, UserDTO, UserRole } from '@rebrowse/types';
import { Block } from 'baseui/block';
import { useStyletron } from 'baseui';
import { SIZE } from 'baseui/input';

import { DeleteOrganization } from './DeleteOrganization';
import { SetupAvatar } from './SetupAvatar';

const PATH: Path = [
  SETTINGS_PATH_PART,
  ORGANIZATION_SETTINGS_PAGE_PART,
  ORGANIZATION_SETTINGS_GENERAL_PAGE_PART,
];

type Props = {
  organization: OrganizationDTO;
  user: UserDTO;
};

export const OrganizationSettingsGeneralPage = ({
  organization: initialOrganization,
  user: initialUser,
}: Props) => {
  const [_css, theme] = useStyletron();
  const { user } = useUser(initialUser);
  const { organization, update, updateAvatar } = useOrganization(
    initialOrganization
  );

  const {
    value: name,
    setValue: setName,
    updating: updatingName,
    updateCurrent: maybeUpdateName,
  } = useUpdateField({
    fieldName: 'name',
    currentValue: organization.name,
    resource: 'organization',
    update,
  });

  const {
    value: defaultRole,
    setValue: setDefaultRole,
    updating: updatingDefaultRole,
    updateCurrent: maybeUpdateDefaultRole,
  } = useUpdateField({
    fieldName: 'defaultRole',
    currentValue: organization.defaultRole,
    resource: 'organization',
    update,
  });

  const {
    value: openMembership,
    updating: updatingOpenMembership,
    updateNext: updateMembership,
  } = useUpdateField({
    fieldName: 'openMembership',
    currentValue: organization.openMembership,
    resource: 'organization',
    update,
  });

  return (
    <OrganizationSettingsPageLayout
      user={user}
      organization={organization}
      path={PATH}
      header="Organization settings"
      title="General"
    >
      <Panel>
        <Panel.Header>General</Panel.Header>
        <Panel.Item display="flex" justifyContent="space-between">
          <Panel.Label
            width="50%"
            explanation="A unique ID used to identify this organization"
            for="id"
          >
            Organization ID
          </Panel.Label>

          <VerticalAligned width="50%">
            <Input
              value={organization.id}
              id="id"
              name="id"
              placeholder="Organization ID"
              disabled
              size={SIZE.compact}
            />
          </VerticalAligned>
        </Panel.Item>

        <Panel.Item display="flex" justifyContent="space-between">
          <Panel.Label
            width="50%"
            explanation="A human-friendly name for the organization"
            for="name"
          >
            Display Name
          </Panel.Label>

          <VerticalAligned width="50%">
            <Input
              onChange={(event) => setName(event.currentTarget.value)}
              value={name}
              id="name"
              name="name"
              placeholder="Display name"
              disabled={updatingName}
              onBlur={() => maybeUpdateName()}
              size={SIZE.compact}
            />
          </VerticalAligned>
        </Panel.Item>

        <Panel.Item display="flex" justifyContent="space-between">
          <Panel.Label
            width="50%"
            explanation="Date of creation"
            for="createdAt"
          >
            Created at
          </Panel.Label>

          <VerticalAligned width="50%">
            <Input
              placeholder="Created at"
              value={organization.createdAt.toUTCString()}
              disabled
              name="createdAt"
              id="createdAt"
              size={SIZE.compact}
            />
          </VerticalAligned>
        </Panel.Item>

        <Panel.Item display="flex" justifyContent="space-between">
          <Panel.Label
            explanation="Date of last update"
            for="updatedAt"
            width="50%"
          >
            Updated at
          </Panel.Label>

          <VerticalAligned width="50%">
            <Input
              placeholder="Updated at"
              value={organization.updatedAt.toUTCString()}
              disabled
              name="updatedAt"
              id="updatedAt"
              size={SIZE.compact}
            />
          </VerticalAligned>
        </Panel.Item>
      </Panel>

      <Panel marginTop={theme.sizing.scale800}>
        <Panel.Header>Membership</Panel.Header>
        <Panel.Item>
          <SpacedBetween>
            <Panel.Label
              width="50%"
              explanation="The default role new members will receive"
              for="defaultRole"
            >
              Default role
            </Panel.Label>

            <VerticalAligned width="50%">
              <AutocompleteInput
                placeholder="Default role"
                options={[
                  { label: 'Member', value: 'member' },
                  { label: 'Admin', value: 'admin' },
                ]}
                value={defaultRole}
                onChange={(value) => setDefaultRole(value as UserRole)}
                onBlur={() => maybeUpdateDefaultRole()}
                isLoading={updatingDefaultRole}
                disabled={updatingDefaultRole}
                size={SIZE.compact}
              />
            </VerticalAligned>
          </SpacedBetween>
        </Panel.Item>

        <Panel.Item>
          <SpacedBetween>
            <Panel.Label
              width="50%"
              for="openMembership"
              explanation="Allow users to freely join the organization using SSO instead of requiring an invite"
            >
              Open membership
            </Panel.Label>

            <VerticalAligned width="50%">
              <Block width="fit-content">
                <Toggle
                  id="openMembership"
                  name="openMembership"
                  checked={openMembership}
                  disabled={updatingOpenMembership}
                  onChange={() => updateMembership(!openMembership)}
                />
              </Block>
            </VerticalAligned>
          </SpacedBetween>
        </Panel.Item>
      </Panel>

      <Panel marginTop={theme.sizing.scale800}>
        <Panel.Header>Avatar</Panel.Header>
        <Panel.Item>
          <SetupAvatar
            organization={organization}
            updateAvatar={updateAvatar}
          />
        </Panel.Item>
      </Panel>

      <Panel marginTop={theme.sizing.scale800}>
        <Panel.Header>Termination</Panel.Header>
        <Panel.Item>
          <SpacedBetween>
            <Panel.Label
              width="50%"
              explanation="Removing organization will delete all data including projects and their associated events"
            >
              Delete organization
            </Panel.Label>
            <VerticalAligned width="50%">
              <Block width="fit-content">
                <DeleteOrganization />
              </Block>
            </VerticalAligned>
          </SpacedBetween>
        </Panel.Item>
      </Panel>
    </OrganizationSettingsPageLayout>
  );
};
