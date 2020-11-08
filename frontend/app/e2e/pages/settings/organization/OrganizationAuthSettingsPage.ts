import { queryByPlaceholderText, queryByText } from '@testing-library/testcafe';

import { ORGANIZATION_SETTINGS_AUTH_PAGE } from '../../../../src/shared/constants/routes';

import { AbstractOrganizationSettingsPage } from './AbstractOrganizationSettingsPage';

type SsoMethodWithConfigurationEndpointText = 'Okta' | 'OneLogin' | 'Auth0';
type SsoMethodWithoutConfigurationEndpointText =
  | 'Google'
  | 'Active directory'
  | 'Github';

type SetupSsoParams =
  | {
      label: SsoMethodWithConfigurationEndpointText;
      metadataEndpoint: string;
    }
  | {
      label: SsoMethodWithoutConfigurationEndpointText;
      metadataEndpoint?: undefined;
    };

export class OrganizationAuthSettingsPage extends AbstractOrganizationSettingsPage {
  public readonly OKTA_METADATA_ENDPOINT =
    'https://snuderlstest.okta.com/app/exkligrqDovHJsGmk5d5/sso/saml/metadata';

  public readonly header = this.withinContainer.queryByText('Authentication');
  public readonly metadataInput = queryByPlaceholderText(
    this.OKTA_METADATA_ENDPOINT
  );
  public readonly enableButton = queryByText('Enable');
  public readonly nonBusinessEmailErrorMessage = queryByText(
    'SSO setup is only possible for work domain.'
  );

  public ssoSetupCompleteMessage = (label: string) => {
    return queryByText(`${label} SSO setup enabled`);
  };

  public getToggleInput = (label: string) => {
    return this.withinContainer
      .queryByText(label)
      .parent()
      .parent()
      .parent()
      .find('input')
      .parent();
  };

  public setupSso = (
    t: TestController,
    { label, metadataEndpoint }: SetupSsoParams
  ) => {
    let base = t.click(this.getToggleInput(label));

    if (metadataEndpoint) {
      base = base.typeText(this.metadataInput, metadataEndpoint);
    }

    return base
      .click(this.enableButton)
      .expect(this.ssoSetupCompleteMessage(label).visible)
      .ok('SSO setup enabled');
  };
}

export default new OrganizationAuthSettingsPage(
  ORGANIZATION_SETTINGS_AUTH_PAGE
);
