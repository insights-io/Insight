import { queryByText } from '@testing-library/testcafe';
import jsQR from 'jsqr';
import { Selector } from 'testcafe';

import { VerificationPage } from '../..';
import { getImageData } from '../../../utils';

export class TimeBasedTwoFactorAuthenticationSetupModal {
  public readonly title = queryByText('Setup multi-factor authentication');

  public extractAuthenticatorTfaCode = async (t: TestController) => {
    const querySelector = 'img[alt="Time-based one-time password QR code"]';
    await t
      .expect(Selector(querySelector, { timeout: 5000 }).visible)
      .ok('QR Image is visible');

    const imageData = await getImageData(querySelector);
    const qrCode = jsQR(imageData.data, imageData.width, imageData.height);
    const tfaSecret = qrCode.data.split('?secret=')[1];
    return tfaSecret;
  };

  public readonly codeInput = VerificationPage.codeInput;
  public readonly submitButton = VerificationPage.submitButton;
  public readonly invalidCodeError = VerificationPage.invalidCodeError;
}

export default new TimeBasedTwoFactorAuthenticationSetupModal();
