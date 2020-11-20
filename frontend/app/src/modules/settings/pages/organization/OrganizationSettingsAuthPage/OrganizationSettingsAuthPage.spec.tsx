import React from 'react';
import { sandbox } from '@rebrowse/testing';
import { render } from 'test/utils';
import userEvent from '@testing-library/user-event';
import { screen } from '@testing-library/react';

import { WithSaml } from './OrganizationSettingsAuthPage.stories';

describe('<OrganizationSettingsAuthPage />', () => {
  it('User should be able to change SSO after initial setup', async () => {
    WithSaml.story.setupMocks(sandbox);
    render(<WithSaml />);

    const toggle = screen
      .getByText('Enable your organization to sign in with Okta.')
      .parentElement?.parentElement?.parentElement?.querySelector(
        'input'
      ) as HTMLInputElement;

    expect(toggle).toBeChecked();
    userEvent.click(toggle);

    await screen.findByText('Disable Okta authentication');
  });
});
