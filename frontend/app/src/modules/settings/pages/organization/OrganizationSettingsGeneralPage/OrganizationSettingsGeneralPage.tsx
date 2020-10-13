import React, { useState } from 'react';
import {
  SETTINGS_PATH_PART,
  ORGANIZATION_SETTINGS_PAGE_PART,
  ORGANIZATION_SETTINGS_GENERAL_PAGE_PART,
} from 'shared/constants/routes';
import { OrganizationSettingsPageLayout } from 'modules/settings/components/organization/OrganizationSettingsPageLayout';
import type { Path } from 'modules/settings/types';
import type { OrganizationDTO, UserDTO } from '@insight/types';
import { OrganizationInfoTable } from 'modules/settings/components/organization/OrganizationInfoTable';
import { useOrganization } from 'shared/hooks/useOrganization';
import { useUser } from 'shared/hooks/useUser';
import { Panel, VerticalAligned, Label, Input } from '@insight/elements';
import { ParagraphXSmall } from 'baseui/typography';
import { toaster } from 'baseui/toast';

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
  const { user } = useUser(initialUser);
  const { organization, updateOrganization } = useOrganization(
    initialOrganization
  );
  const [name, setName] = useState(organization.name);
  const [updatingName, setUpdatingName] = useState(false);

  const maybeUpdateName = () => {
    if (name === organization.name) {
      return;
    }

    setUpdatingName(true);
    updateOrganization({ name })
      .then((updatedOrganization) =>
        toaster.positive(
          `Changed organization name from "${organization.name}" to "${updatedOrganization.name}"`,
          {}
        )
      )
      .catch(() =>
        toaster.negative(
          'Something went wrong while trying to update organization',
          {}
        )
      )
      .finally(() => setUpdatingName(false));
  };

  return (
    <OrganizationSettingsPageLayout
      user={user}
      organization={organization}
      path={PATH}
      header="Organization settings"
    >
      <Panel marginBottom="24px">
        <Panel.Header>General</Panel.Header>
        <Panel.Item display="flex" justifyContent="space-between">
          <VerticalAligned
            as="label"
            overrides={{ Block: { props: { for: 'id' } } }}
          >
            <Label as="div" required>
              Organization ID
            </Label>
            <ParagraphXSmall margin={0}>
              A unique ID used to identify this organization
            </ParagraphXSmall>
          </VerticalAligned>
          <VerticalAligned width="50%">
            <Input value={organization.id} disabled id="id" />
          </VerticalAligned>
        </Panel.Item>

        <Panel.Item display="flex" justifyContent="space-between">
          <VerticalAligned
            as="label"
            overrides={{ Block: { props: { for: 'name' } } }}
          >
            <Label as="div" required>
              Display Name
            </Label>
            <ParagraphXSmall margin={0}>
              A human-friendly name for the organization
            </ParagraphXSmall>
          </VerticalAligned>
          <VerticalAligned width="50%">
            <Input
              onChange={(event) => setName(event.currentTarget.value)}
              value={name}
              id="name"
              disabled={updatingName}
              onBlur={maybeUpdateName}
            />
          </VerticalAligned>
        </Panel.Item>
      </Panel>

      <OrganizationInfoTable organization={organization} />
    </OrganizationSettingsPageLayout>
  );
};
