import { sandbox } from '@insight/testing';
import React from 'react';
import { SSO_SAML_SETUP } from 'test/data';
import { render } from 'test/utils';

import { WithSaml } from './OrganizationSettingsAuthPage.stories';

describe('<OrganizationSettingsAuthPage />', () => {
  it('User should be able to change SSO after initial setup', async () => {
    WithSaml.story.setupMocks(sandbox);
    const { getByPlaceholderText, container } = render(<WithSaml />);

    const methodInput = container.querySelector(
      'input[aria-label="Selected SAML. "]'
    );
    const configurationInput = getByPlaceholderText(
      'https://example.okta.com/app/exkw843tlucjMJ0kL4x6/sso/saml/metadata'
    ) as HTMLInputElement;

    expect(methodInput).toBeInTheDocument();
    expect(configurationInput.value).toEqual(
      SSO_SAML_SETUP.configurationEndpoint
    );
  });
});
