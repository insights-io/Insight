import React from 'react';
import { render } from 'test/utils';
import { sandbox } from '@rebrowse/testing';
import userEvent from '@testing-library/user-event';
import type { StoryConfiguration } from '@rebrowse/storybook';
import type { RenderableComponent } from '@rebrowse/next-testing';
import { screen, waitFor } from '@testing-library/react';
import { TOTP_MFA_SETUP_DTO } from 'test/data';

import {
  Base,
  WithSetupStartError,
  WithInvalidCodeError,
  WithQrCodeExpiredError,
} from './TotpMfaSetupModal.stories';

describe('<TotpMfaSetupModal />', () => {
  const renderModal = <Props, T, S extends StoryConfiguration<T>>(
    component: RenderableComponent<Props, T, S>
  ) => {
    const result = render(component);

    const closeButton = result.container.querySelector(
      'button[aria-label="Close"]'
    ) as HTMLButtonElement;

    const codeInput = result.container.querySelector(
      'input[aria-label="Please enter your pin code"]'
    ) as HTMLInputElement;

    const submitButton = screen.getByText('Submit');
    return { submitButton, codeInput, closeButton };
  };

  it('Base', async () => {
    const { completeSetup, setupStart } = Base.story.setupMocks(sandbox);
    const onClose = sandbox.stub();
    const onCompleted = sandbox.stub();

    const { submitButton, codeInput, closeButton } = renderModal(
      <Base onClose={onClose} onCompleted={onCompleted} />
    );

    userEvent.click(closeButton);
    sandbox.assert.calledOnce(onClose);
    sandbox.assert.calledWithExactly(setupStart);

    await userEvent.type(codeInput, '12');
    userEvent.click(submitButton);
    await screen.findByText('Required');

    const code = '123456';
    await userEvent.type(codeInput, code);
    expect(screen.queryByText('Required')).toBeNull();

    userEvent.click(submitButton);
    sandbox.assert.calledWithExactly(completeSetup, 'totp', Number(code));

    await waitFor(() => {
      sandbox.assert.calledWithExactly(onCompleted, TOTP_MFA_SETUP_DTO);
    });
  });

  it('WithSetupStartError', async () => {
    WithSetupStartError.story.setupMocks(sandbox);
    const { submitButton, codeInput } = renderModal(<WithSetupStartError />);
    await screen.findByText('Internal Server Error');

    expect(submitButton).toBeDisabled();
    expect(codeInput).toBeDisabled();
  });

  it('WithInvalidCodeError', async () => {
    WithInvalidCodeError.story.setupMocks(sandbox);
    const { submitButton, codeInput } = renderModal(<WithInvalidCodeError />);
    await userEvent.type(codeInput, '123456');
    userEvent.click(submitButton);
    await screen.findByText('Invalid code');

    expect(submitButton).not.toBeDisabled();
    expect(codeInput).not.toBeDisabled();
  });

  it('WithQrCodeExpiredError', async () => {
    WithQrCodeExpiredError.story.setupMocks(sandbox);
    const { submitButton, codeInput } = renderModal(<WithQrCodeExpiredError />);
    await userEvent.type(codeInput, '123456');
    userEvent.click(submitButton);
    await screen.findByText('QR code expired');

    expect(submitButton).not.toBeDisabled();
    expect(codeInput).not.toBeDisabled();
  });
});
