import { queryByText, within } from '@testing-library/testcafe';
import jsQR from 'jsqr';

import config from '../config';
import { getImageData } from '../utils';

import Verification from './Verification';

class AccountSettings {
  public readonly path = `${config.appBaseURL}/account/settings`;

  /* Selectors */
  private readonly container = within('div.account-settings');
  public readonly title = this.container.queryByText('Account settings');

  public readonly tfa = {
    codeInput: Verification.codeInput,
    submitButton: Verification.submitButton,
    invalidCodeError: Verification.invalidCodeError,
    disableSubmitButton: queryByText('Yes'),
    sms: {
      checkbox: this.container.queryByText('Text message'),
      disabledToast: queryByText(
        'Text message two factor authentication disabled'
      ),
      enabledToast: queryByText(
        'Text message two factor authentication enabled'
      ),
    },
    totp: {
      checkbox: this.container.queryByText('Authy / Google Authenticator'),
      disabledToast: queryByText(
        'Authy / Google Authenticator two factor authentication disabled'
      ),
      enabledToast: queryByText(
        'Authy / Google Authenticator two factor authentication enabled'
      ),
      extractQrCodeSecret: async () => {
        const imageData = await getImageData('img[alt="TFA QR code"]');
        const qrCode = jsQR(imageData.data, imageData.width, imageData.height);
        const tfaSecret = qrCode.data.split('?secret=')[1];
        return tfaSecret;
      },
    },
  };
}

export default new AccountSettings();
