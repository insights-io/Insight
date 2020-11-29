import React from 'react';
import { sandbox } from '@rebrowse/testing';
import { screen, waitFor } from '@testing-library/react';
import { render } from 'test/utils';
import userEvent from '@testing-library/user-event';

import {
  MfaDisabled,
  MfaEnabled,
  WithError,
} from './AccountSettingsSecurityPage.stories';

const getToggleByText = (text: string) => {
  return screen
    .getByText(text)
    .parentElement?.parentElement?.querySelector(
      'input[type="checkbox"]'
    ) as HTMLInputElement;
};

describe('<AccountSettingsSecurityPage />', () => {
  it('Should render enabled', async () => {
    const { listSetups } = MfaEnabled.story.setupMocks(sandbox);
    render(<MfaEnabled />);

    const authenticatorAppToggle = getToggleByText(
      'Authy / Google Authenticator'
    );
    const textMessageToggle = getToggleByText('Text message');

    await waitFor(() => {
      sandbox.assert.calledWithExactly(listSetups);
    });

    expect(authenticatorAppToggle).toHaveAttribute('aria-checked', 'true');
    expect(textMessageToggle).toHaveAttribute('aria-checked', 'true');

    userEvent.click(authenticatorAppToggle);

    await screen.findByText(
      'Are you sure you want to disable multi-factor authentication method?'
    );
  });

  it('Should render disabled', async () => {
    const { listSetups } = MfaDisabled.story.setupMocks(sandbox);
    render(<MfaDisabled />);

    const authenticatorAppCheckbox = getToggleByText(
      'Authy / Google Authenticator'
    );
    const textMessageCheckbox = getToggleByText('Text message');

    await waitFor(() => {
      sandbox.assert.calledWithExactly(listSetups);
    });

    expect(authenticatorAppCheckbox).toHaveAttribute('aria-checked', 'false');
    expect(textMessageCheckbox).toHaveAttribute('aria-checked', 'false');

    userEvent.click(authenticatorAppCheckbox);

    await screen.findByText('Scan QR code to start');
  });

  it('Should render disabled on error', async () => {
    const { listSetups } = WithError.story.setupMocks(sandbox);
    render(<WithError />);

    const authenticatorAppToggle = getToggleByText(
      'Authy / Google Authenticator'
    );
    const textMessageToggle = getToggleByText('Text message');

    await waitFor(() => {
      sandbox.assert.calledWithExactly(listSetups);
    });

    expect(authenticatorAppToggle).toHaveAttribute('disabled');
    expect(authenticatorAppToggle).toHaveAttribute('aria-checked', 'false');

    expect(textMessageToggle).toHaveAttribute('disabled');
    expect(textMessageToggle).toHaveAttribute('aria-checked', 'false');
  });
});
