import React from 'react';
import {
  SETTINGS_PATH_PART,
  ORGANIZATION_SETTINGS_PAGE_PART,
  ORGANIZATION_SETTINGS_AUTH_PAGE_PART,
} from 'shared/constants/routes';
import { OrganizationSettingsPageLayout } from 'settings/components/organization/OrganizationSettingsPageLayout';
import { useSsoSetup } from 'settings/hooks/useSsoSetup';
import { useUser } from 'shared/hooks/useUser';
import { useOrganization } from 'shared/hooks/useOrganization';
import { Panel } from '@rebrowse/elements';
import type { Path } from 'settings/types';
import type { OrganizationDTO, SsoSetupDTO, UserDTO } from '@rebrowse/types';
import { cdnLogo } from 'shared/utils/assets';

import { SsoProvider } from './SsoProvider';

const PATH: Path = [
  SETTINGS_PATH_PART,
  ORGANIZATION_SETTINGS_PAGE_PART,
  ORGANIZATION_SETTINGS_AUTH_PAGE_PART,
];

type Props = {
  maybeSsoSetup: SsoSetupDTO | undefined;
  user: UserDTO;
  organization: OrganizationDTO;
};

export const OrganizationSettingsAuthPage = ({
  maybeSsoSetup: initialMaybeSsoSetup,
  user: initialUser,
  organization: initialOrganization,
}: Props) => {
  const { maybeSsoSetup, deleteSsoSetup, createSsoSetup } = useSsoSetup(
    initialMaybeSsoSetup
  );
  const { user } = useUser(initialUser);
  const { organization } = useOrganization(initialOrganization);

  return (
    <OrganizationSettingsPageLayout
      path={PATH}
      header="Authentication"
      user={user}
      organization={organization}
    >
      <Panel>
        <Panel.Header>Choose a provider</Panel.Header>
        <Panel.Item>
          <SsoProvider
            method="google"
            label="Google"
            image={cdnLogo('google.svg')}
            activeSetup={maybeSsoSetup}
            deleteSsoSetup={deleteSsoSetup}
            createSsoSetup={createSsoSetup}
          />
        </Panel.Item>

        <Panel.Item>
          <SsoProvider
            method="github"
            label="Github"
            image={cdnLogo('github.svg')}
            activeSetup={maybeSsoSetup}
            deleteSsoSetup={deleteSsoSetup}
            createSsoSetup={createSsoSetup}
          />
        </Panel.Item>

        <Panel.Item>
          <SsoProvider
            method="microsoft"
            label="Active directory"
            image={cdnLogo('microsoft.svg')}
            activeSetup={maybeSsoSetup}
            deleteSsoSetup={deleteSsoSetup}
            createSsoSetup={createSsoSetup}
          />
        </Panel.Item>

        <Panel.Item>
          <SsoProvider
            method="saml"
            samlMethod="okta"
            label="Okta"
            image={cdnLogo('okta.svg')}
            activeSetup={maybeSsoSetup}
            deleteSsoSetup={deleteSsoSetup}
            createSsoSetup={createSsoSetup}
          />
        </Panel.Item>

        <Panel.Item>
          <SsoProvider
            method="saml"
            samlMethod="onelogin"
            label="OneLogin"
            image={cdnLogo('onelogin.svg')}
            activeSetup={maybeSsoSetup}
            deleteSsoSetup={deleteSsoSetup}
            createSsoSetup={createSsoSetup}
          />
        </Panel.Item>

        <Panel.Item>
          <SsoProvider
            method="saml"
            samlMethod="auth0"
            label="Auth0"
            image={cdnLogo('auth0.svg')}
            activeSetup={maybeSsoSetup}
            deleteSsoSetup={deleteSsoSetup}
            createSsoSetup={createSsoSetup}
          />
        </Panel.Item>
      </Panel>
    </OrganizationSettingsPageLayout>
  );
};
