/* eslint-disable max-classes-per-file */
import { queryByText, TestcafeBoundFunctions } from '@testing-library/testcafe';
import { queries } from '@testing-library/react';

import VerificationPage from '../../Verification';
import { ACCOUNT_SETTINGS_SECURITY_PAGE } from '../../../../src/shared/constants/routes';

import { AbstractAccountSettingsPage } from './AbstractAccountSettingsPage';
import TimeBasedTwoFactorAuthenticationSetupModal from './TimeBasedTwoFactorAuthenticationSetupModal';
import DisableTwoFactorAuthenticationModal from './DisableTwoFactorAuthenticationModal';

class TwoFactorAuthentication {
  public readonly disableModal = DisableTwoFactorAuthenticationModal;

  public readonly authenticatorTfaCheckbox: SelectorPromise;

  public readonly authenticatorTfaDisabledToast = queryByText(
    /Authy \/ Google Authenticator two factor authentication disabled/
  );

  public readonly authenticatorTfaEnabledToast = queryByText(
    /Authy \/ Google Authenticator two factor authentication enabled/
  );
  public readonly authenticatorSetupModal = TimeBasedTwoFactorAuthenticationSetupModal;

  public setupAuthenticatorTFA = (t: TestController) => {
    return this.authenticatorSetupModal
      .extractAuthenticatorTfaCode(t)
      .then((secret) =>
        VerificationPage.completeTotpChallenge(t, secret).then(() => secret)
      );
  };

  public readonly textMessageCheckbox: SelectorPromise;
  public readonly textMessageDisabledTooltipText = queryByText(
    'Verify your phone number to enable text message two factor authentication'
  );
  public readonly textMessageDisabledToastMessage = queryByText(
    'Text message two factor authentication disabled'
  );
  public readonly textMessageEnabledToastMessage = queryByText(
    'Text message two factor authentication enabled'
  );
  public setupTextMessageTFA = (t: TestController) =>
    VerificationPage.completeSmsChallenge(t);

  constructor(container: TestcafeBoundFunctions<typeof queries>) {
    this.authenticatorTfaCheckbox = container.queryByText(
      'Authy / Google Authenticator'
    );
    this.textMessageCheckbox = container.queryByText('Text message');
  }
}

type ChangePasswordParams = {
  currentPassword: string;
  newPassword: string;
  newPasswordConfirm: string;
};

class ChangePassword {
  public readonly currentPasswordInput: SelectorPromise;
  public readonly newPasswordInput: SelectorPromise;
  public readonly confirmNewPasswordInput: SelectorPromise;
  public readonly changePasswordButton: SelectorPromise;

  public readonly passwordChangedMessage = queryByText('Password changed');
  public readonly newPasswordSameAsOldErrorMessage = queryByText(
    'New password cannot be the same as the previous one!'
  );
  public readonly passwordMissmatchErrorMessage = queryByText(
    'Current password miss match'
  );

  constructor(container: TestcafeBoundFunctions<typeof queries>) {
    this.currentPasswordInput = container.queryByPlaceholderText(
      'Current password'
    );
    this.newPasswordInput = container.queryByPlaceholderText('New password');
    this.confirmNewPasswordInput = container.queryByPlaceholderText(
      'Confirm new password'
    );

    this.changePasswordButton = container.queryByText('Save new password');
  }

  public clearInputs = (t: TestController) => {
    return t
      .selectText(this.currentPasswordInput)
      .pressKey('delete')
      .selectText(this.newPasswordInput)
      .pressKey('delete')
      .selectText(this.confirmNewPasswordInput)
      .pressKey('delete');
  };

  public changePassword = (
    t: TestController,
    { currentPassword, newPassword, newPasswordConfirm }: ChangePasswordParams
  ) => {
    return t
      .typeText(this.currentPasswordInput, currentPassword)
      .typeText(this.newPasswordInput, newPassword)
      .typeText(this.confirmNewPasswordInput, newPasswordConfirm)
      .click(this.changePasswordButton);
  };
}

export class AccountSettingsDetailsPage extends AbstractAccountSettingsPage {
  public readonly title = this.withinContainer.queryByText('Security');

  public readonly tfa = new TwoFactorAuthentication(this.withinContainer);
  public readonly changePassword = new ChangePassword(this.withinContainer);
}

export default new AccountSettingsDetailsPage(ACCOUNT_SETTINGS_SECURITY_PAGE);
