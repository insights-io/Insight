import React from 'react';
import { render } from 'test/utils';
import { sandbox } from '@rebrowse/testing';
import userEvent from '@testing-library/user-event';
import { screen, waitFor } from '@testing-library/react';
import { TOTP_MFA_SETUP_DTO } from 'test/data/mfa';

import {
  Base,
  WithSetupStartError,
  WithInvalidCodeError,
  WithQrCodeExpiredError,
} from './TotpMfaSetupModal.stories';

describe('<TotpMfaSetupModal />', () => {
  it('Base', async () => {
    const { completeSetup, setupStart } = Base.story.setupMocks(sandbox);
    const onClose = sandbox.stub();
    const onCompleted = sandbox.stub();

    const { container } = render(
      <Base onClose={onClose} onCompleted={onCompleted} />
    );

    userEvent.click(screen.getByLabelText('Close'));
    sandbox.assert.calledOnce(onClose);
    sandbox.assert.calledWithExactly(setupStart);

    const codeInput = container.querySelector(
      'input[aria-label="Please enter your pin code"]'
    ) as HTMLInputElement;

    await userEvent.type(codeInput, '12');
    userEvent.click(screen.getByText('Submit'));
    await screen.findByText('Required');

    const code = '123456';
    await userEvent.type(codeInput, code);
    expect(screen.queryByText('Required')).toBeNull();

    userEvent.click(screen.getByText('Submit'));
    sandbox.assert.calledWithExactly(completeSetup, 'totp', Number(code));

    await waitFor(() => {
      sandbox.assert.calledWithExactly(onCompleted, TOTP_MFA_SETUP_DTO);
    });
  });

  it('WithSetupStartError', async () => {
    WithSetupStartError.story.setupMocks(sandbox);
    const { container } = render(<WithSetupStartError />);

    const codeInput = container.querySelector(
      'input[aria-label="Please enter your pin code"]'
    ) as HTMLInputElement;

    await screen.findByText('Internal Server Error');

    expect(screen.getByText('Submit')).toBeDisabled();
    expect(codeInput).toBeDisabled();
  });

  it('WithInvalidCodeError', async () => {
    WithInvalidCodeError.story.setupMocks(sandbox);
    const { container } = render(<WithInvalidCodeError />);

    const codeInput = container.querySelector(
      'input[aria-label="Please enter your pin code"]'
    ) as HTMLInputElement;

    await userEvent.type(codeInput, '123456');
    userEvent.click(screen.getByText('Submit'));
    await screen.findByText('Invalid code');

    expect(screen.getByText('Submit')).not.toBeDisabled();
    expect(codeInput).not.toBeDisabled();
  });

  it('WithQrCodeExpiredError', async () => {
    WithQrCodeExpiredError.story.setupMocks(sandbox);
    const { container } = render(<WithQrCodeExpiredError />);

    const codeInput = container.querySelector(
      'input[aria-label="Please enter your pin code"]'
    ) as HTMLInputElement;

    await userEvent.type(codeInput, '123456');
    userEvent.click(screen.getByText('Submit'));
    await screen.findByText('QR code expired');

    expect(screen.getByText('Submit')).not.toBeDisabled();
    expect(codeInput).not.toBeDisabled();
  });
});
