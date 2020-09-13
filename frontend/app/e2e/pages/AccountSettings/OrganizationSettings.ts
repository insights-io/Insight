import { queryByText, TestcafeBoundFunctions } from '@testing-library/testcafe';
import { queries } from '@testing-library/react';

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

export const OrganizationSettings = (
  container: TestcafeBoundFunctions<typeof queries>
) => {
  const generalTabButton = container.queryByText('General');
  const securityTabButton = container.queryByText('Security');
  const ssoSubmitButton = container.queryByText('Setup');
  const ssoSetupCompleteMessage = queryByText('SSO setup complete');
  const ssoConfigurationEndpointInput = container.queryByPlaceholderText(
    'https://example.okta.com/app/exkw843tlucjMJ0kL4x6/sso/saml/metadata'
  );

  const setupSso = (
    t: TestController,
    { from, to, configurationEndpoint }: SetupSsoParams
  ) => {
    let base = t.click(securityTabButton);

    if (from) {
      base = base.click(queryByText(from));
    }

    if (to) {
      base = base.click(queryByText(to));
    }

    if (configurationEndpoint) {
      base = base.typeText(
        ssoConfigurationEndpointInput,
        configurationEndpoint
      );
    }

    return base
      .click(ssoSubmitButton)
      .expect(ssoSetupCompleteMessage.visible)
      .ok('SSO setup complete');
  };

  return {
    tabs: {
      general: {
        button: generalTabButton,
      },
      security: {
        button: securityTabButton,
        sso: {
          setup: setupSso,
          setupCompleteMessage: ssoSetupCompleteMessage,
          nonBusinessEmailErrorMessage: container.queryByText(
            'SSO setup is only possible for work domain.'
          ),
          configurationEndpointInput: ssoConfigurationEndpointInput,
          submitButton: ssoSubmitButton,
        },
      },
    },
  };
};
