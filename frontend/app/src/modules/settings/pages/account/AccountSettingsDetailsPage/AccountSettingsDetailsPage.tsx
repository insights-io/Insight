import React from 'react';
import { AccountSettingsPageLayout } from 'modules/settings/components/account/AccountSettingsPageLayout';
import {
  ACCOUNT_SETTINGS_DETAILS_PAGE_PART,
  SETTINGS_PATH_PART,
  ACCOUNT_SETTINGS_PATH_PART,
} from 'shared/constants/routes';
import { AccountInfoTable } from 'modules/settings/components/account/AccountInfoTable';
import { useUser } from 'shared/hooks/useUser';
import type { Path } from 'modules/settings/types';
import type { OrganizationDTO, UserDTO } from '@rebrowse/types';
import { useOrganization } from 'shared/hooks/useOrganization';

const PATH: Path = [
  SETTINGS_PATH_PART,
  ACCOUNT_SETTINGS_PATH_PART,
  ACCOUNT_SETTINGS_DETAILS_PAGE_PART,
];

type Props = {
  user: UserDTO;
  organization: OrganizationDTO;
};

export const AccountSettingsDetailsPage = ({
  user: initialUser,
  organization: initialOrganization,
}: Props) => {
  const { user, updatePhoneNumber, setUser } = useUser(initialUser);
  const { organization } = useOrganization(initialOrganization);

  return (
    <AccountSettingsPageLayout
      user={user}
      organization={organization}
      path={PATH}
      header="Account details"
      title="Details"
    >
      <AccountInfoTable
        user={user}
        updatePhoneNumber={updatePhoneNumber}
        setUser={setUser}
      />
    </AccountSettingsPageLayout>
  );
};
