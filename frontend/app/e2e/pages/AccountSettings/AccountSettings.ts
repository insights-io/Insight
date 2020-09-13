import {
  queryByPlaceholderText,
  queryByText,
  within,
} from '@testing-library/testcafe';
import jsQR from 'jsqr';
import { Selector } from 'testcafe';

import config from '../../config';
import { getImageData } from '../../utils';
import VerificationPage from '../Verification';

import ChangePassword from './ChangePassword';
import TeamInvite from './TeamInvite';
import { OrganizationSettings } from './OrganizationSettings';

class AccountSettings {
  public readonly path = `${config.appBaseURL}/account/settings`;

  /* Selectors */
  private readonly container = within('div.account-settings');

  public readonly title = this.container.queryByText('Account settings');

  public readonly tabs = {
    userSettings: this.container.queryByText('User settings'),
    organizationSettings: this.container.queryByText('Organization settings'),
  };

  public readonly OrganizationSettings = OrganizationSettings(this.container);
  public readonly ChangePassword = ChangePassword;
  public readonly TeamInvite = TeamInvite;

  public readonly phoneNumber = {
    configureButton: this.container
      .queryByText('Phone number')
      .parent()
      .find('button'),
    input: queryByPlaceholderText('Phone number'),
    nextStep: queryByText('Next'),
    verifiedMessage: queryByText('Phone number verified'),
  };

  public verifyCurrentPhoneNumber = async (t: TestController) => {
    await t
      .click(this.phoneNumber.configureButton)
      .click(this.phoneNumber.nextStep);

    await VerificationPage.completeSmsChallenge(t);
    return t
      .expect(queryByText('Phone number verified').visible)
      .ok('Success message');
  };

  public readonly tfa = {
    codeInput: VerificationPage.codeInput,
    submitButton: VerificationPage.submitButton,
    invalidCodeError: VerificationPage.invalidCodeError,
    disableSubmitButton: queryByText('Yes'),
    sms: {
      disabledText: queryByText(
        'Verify your phone number to enable text message two factor authentication'
      ),
      checkbox: this.container.queryByText('Text message'),
      disabledToast: queryByText(
        'Text message two factor authentication disabled'
      ),
      enabledToast: queryByText(
        'Text message two factor authentication enabled'
      ),
      setup: (t: TestController) => VerificationPage.completeSmsChallenge(t),
    },
    totp: {
      checkbox: this.container.queryByText('Authy / Google Authenticator'),
      disabledToast: queryByText(
        /Authy \/ Google Authenticator two factor authentication disabled/
      ),
      enabledToast: queryByText(
        /Authy \/ Google Authenticator two factor authentication enabled/
      ),
      extractQrCodeSecret: async (t: TestController) => {
        const querySelector = 'img[alt="TFA QR code"]';
        await t
          .expect(Selector(querySelector, { timeout: 5000 }).visible)
          .ok('QR Image is visible');
        const imageData = await getImageData(querySelector);
        const qrCode = jsQR(imageData.data, imageData.width, imageData.height);
        const tfaSecret = qrCode.data.split('?secret=')[1];
        return tfaSecret;
      },
      setup: (t: TestController) => {
        return this.tfa.totp
          .extractQrCodeSecret(t)
          .then((secret) =>
            VerificationPage.completeTotpChallenge(t, secret).then(() => secret)
          );
      },
    },
  };
}

export default new AccountSettings();
