import React from 'react';
import { sandbox } from '@rebrowse/testing';
import { screen } from '@testing-library/react';
import { render } from 'test/utils';
import userEvent from '@testing-library/user-event';

import { MfaDisabled, MfaEnabled } from './AccountSettingsSecurityPage.stories';

const getToggleByText = (text: string) => {
  return screen
    .getByText(text)
    .parentElement?.parentElement?.querySelector(
      'input[type="checkbox"]'
    ) as HTMLInputElement;
};

describe('<AccountSettingsSecurityPage />', () => {
  it('As a user I should be able to disable TOTP MFA setup', async () => {
    const { disable } = MfaEnabled.story.setupMocks(sandbox);
    render(<MfaEnabled />);

    const authenticatorAppToggle = getToggleByText(
      'Authy / Google Authenticator'
    );
    const textMessageToggle = getToggleByText('Text message');

    expect(authenticatorAppToggle).toHaveAttribute('aria-checked', 'true');
    expect(textMessageToggle).toHaveAttribute('aria-checked', 'true');

    userEvent.click(authenticatorAppToggle);

    await screen.findByText(
      'Are you sure you want to disable multi-factor authentication method?'
    );

    userEvent.click(screen.getByText('Disable'));

    await screen.findByText(
      'Authy / Google Authenticator multi-factor authentication disabled'
    );
    sandbox.assert.calledWithExactly(disable, 'totp');
    expect(authenticatorAppToggle).toHaveAttribute('aria-checked', 'false');
  });

  it('As a user I should be able to enable TOTP MFA setup', async () => {
    MfaDisabled.story.setupMocks(sandbox);
    const { container } = render(<MfaDisabled />);

    const authenticatorAppCheckbox = getToggleByText(
      'Authy / Google Authenticator'
    );
    const textMessageCheckbox = getToggleByText('Text message');

    expect(authenticatorAppCheckbox).toHaveAttribute('aria-checked', 'false');
    expect(textMessageCheckbox).toHaveAttribute('aria-checked', 'false');

    userEvent.click(authenticatorAppCheckbox);

    await screen.findByText('Scan QR code to start');

    await userEvent.type(
      container.querySelector(
        'input[aria-label="Please enter your pin code"]'
      ) as HTMLInputElement,
      '123456'
    );
    userEvent.click(screen.getByText('Submit'));

    await screen.findByText(
      'Authy / Google Authenticator multi-factor authentication enabled'
    );
    expect(authenticatorAppCheckbox).toHaveAttribute('aria-checked', 'true');
  });
});
