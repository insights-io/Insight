import { queryByText } from '@testing-library/testcafe';

import { ORGANIZATION_SETTINGS_AUTH_PAGE } from '../../../../src/shared/constants/routes';

import { AbstractOrganizationSettingsPage } from './AbstractOrganizationSettingsPage';

type SsoMethodWithConfigurationEndpointText = 'SAML';
type SsoMethodWithoutConfigurationEndpointText =
  | 'Google'
  | 'Microsoft'
  | 'Github';

type SsoMethodText =
  | SsoMethodWithConfigurationEndpointText
  | SsoMethodWithoutConfigurationEndpointText;

type SetupSsoParams =
  | { configurationEndpoint: string; from?: undefined; to?: undefined }
  | {
      from: SsoMethodText;
      to: SsoMethodWithConfigurationEndpointText;
      configurationEndpoint: string;
    }
  | {
      from: SsoMethodText;
      to: SsoMethodWithoutConfigurationEndpointText;
      configurationEndpoint?: undefined;
    };

export class OrganizationAuthSettingsPage extends AbstractOrganizationSettingsPage {
  public readonly header = this.container.queryByText('Authentication');
  public readonly ssoConfigurationEndpointInput = this.container.queryByPlaceholderText(
    'https://example.okta.com/app/exkw843tlucjMJ0kL4x6/sso/saml/metadata'
  );
  public readonly ssoSubmitButton = this.container.queryByText('Setup');
  public readonly nonBusinessEmailErrorMessage = this.container.queryByText(
    'SSO setup is only possible for work domain.'
  );
  public readonly ssoSetupCompleteMessage = queryByText('SSO setup complete');

  public setupSso = (
    t: TestController,
    { from, to, configurationEndpoint }: SetupSsoParams
  ) => {
    let base = t;

    if (from) {
      base = base.click(queryByText(from));
    }

    if (to) {
      base = base.click(queryByText(to));
    }

    if (configurationEndpoint) {
      base = base.typeText(
        this.ssoConfigurationEndpointInput,
        configurationEndpoint
      );
    }

    return base
      .click(this.ssoSubmitButton)
      .expect(this.ssoSetupCompleteMessage.visible)
      .ok('SSO setup complete');
  };
}

export default new OrganizationAuthSettingsPage(
  ORGANIZATION_SETTINGS_AUTH_PAGE
);
