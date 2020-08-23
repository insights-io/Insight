import { queryByText, within } from '@testing-library/testcafe';
import jsQR from 'jsqr';

import config from '../config';
import { getQrImageData } from '../utils/io';

import Verification from './Verification';

class AccountSettings {
  public readonly path = `${config.appBaseURL}/account/settings`;

  /* Selectors */
  private readonly container = within('div.account-settings');
  public readonly title = this.container.queryByText('Account settings');
  public readonly twoFactorAuthentication = this.container.queryByText(
    /Two factor authentication.*/
  );

  public readonly tfa = {
    codeInput: Verification.codeInput,
    submitButton: Verification.submitButton,
    invalidCodeError: Verification.invalidCodeError,
    disabledToast: queryByText(
      'Two factor authentication has been successfully disabled'
    ),
    enabledToast: queryByText(
      'Two factor authentication has been successfully set up'
    ),
    extractQrCodeSecret: async () => {
      const imageData = await getQrImageData('img[alt="TFA QR code"]');
      const qrCode = jsQR(imageData.data, imageData.width, imageData.height);
      const tfaSecret = qrCode.data.split('?secret=')[1];
      return tfaSecret;
    },
  };
}

export default new AccountSettings();
