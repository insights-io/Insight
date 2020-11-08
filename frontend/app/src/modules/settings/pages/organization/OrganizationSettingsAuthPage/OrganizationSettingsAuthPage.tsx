import React from 'react';
import {
  SETTINGS_PATH_PART,
  ORGANIZATION_SETTINGS_PAGE_PART,
  ORGANIZATION_SETTINGS_AUTH_PAGE_PART,
} from 'shared/constants/routes';
import { OrganizationSettingsPageLayout } from 'modules/settings/components/organization/OrganizationSettingsPageLayout';
import { useSsoSetup } from 'modules/settings/hooks/useSsoSetup';
import { useUser } from 'shared/hooks/useUser';
import { useOrganization } from 'shared/hooks/useOrganization';
import { Panel } from '@insight/elements';
import type { Path } from 'modules/settings/types';
import type { OrganizationDTO, SsoSetupDTO, UserDTO } from '@insight/types';

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
  const { maybeSsoSetup, setSsoSetup } = useSsoSetup(initialMaybeSsoSetup);
  const { user } = useUser(initialUser);
  const { organization } = useOrganization(initialOrganization);

  console.log(maybeSsoSetup);

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
            image="https://upload.wikimedia.org/wikipedia/commons/thumb/5/53/Google_%22G%22_Logo.svg/1200px-Google_%22G%22_Logo.svg.png"
            activeSetup={maybeSsoSetup}
            setActiveSetup={setSsoSetup}
          />
        </Panel.Item>

        <Panel.Item>
          <SsoProvider
            method="github"
            label="Github"
            image="https://seeklogo.com/images/G/github-logo-7880D80B8D-seeklogo.com.png"
            activeSetup={maybeSsoSetup}
            setActiveSetup={setSsoSetup}
          />
        </Panel.Item>

        <Panel.Item>
          <SsoProvider
            method="microsoft"
            label="Active directory"
            image="https://s1.sentry-cdn.com/_static/993bc29839537d64c65049472d7b1245/sentry/dist/logo-microsoft.1573e4.svg"
            activeSetup={maybeSsoSetup}
            setActiveSetup={setSsoSetup}
          />
        </Panel.Item>

        <Panel.Item>
          <SsoProvider
            method="saml"
            samlMethod="okta"
            label="Okta"
            image="https://s1.sentry-cdn.com/_static/993bc29839537d64c65049472d7b1245/sentry/dist/logo-okta.19e492.svg"
            activeSetup={maybeSsoSetup}
            setActiveSetup={setSsoSetup}
          />
        </Panel.Item>

        <Panel.Item>
          <SsoProvider
            method="saml"
            samlMethod="onelogin"
            label="OneLogin"
            image="https://s1.sentry-cdn.com/_static/202d96a6babb2914a6c051843bed0776/sentry/dist/logo-onelogin.3e672d.svg"
            activeSetup={maybeSsoSetup}
            setActiveSetup={setSsoSetup}
          />
        </Panel.Item>

        <Panel.Item>
          <SsoProvider
            method="saml"
            samlMethod="auth0"
            label="Auth0"
            image="https://s1.sentry-cdn.com/_static/202d96a6babb2914a6c051843bed0776/sentry/dist/logo-auth0.8d3ad5.svg"
            activeSetup={maybeSsoSetup}
            setActiveSetup={setSsoSetup}
          />
        </Panel.Item>

        <Panel.Item>
          <SsoProvider
            method="saml"
            samlMethod="custom"
            label="SAML 2.0"
            image="https://s1.sentry-cdn.com/_static/993bc29839537d64c65049472d7b1245/sentry/dist/logo-saml2.590c9d.svg"
            activeSetup={maybeSsoSetup}
            setActiveSetup={setSsoSetup}
          />
        </Panel.Item>
      </Panel>
    </OrganizationSettingsPageLayout>
  );
};
