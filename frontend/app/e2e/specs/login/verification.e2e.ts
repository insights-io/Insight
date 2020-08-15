import { queryByText } from '@testing-library/testcafe';
import { Selector, ClientFunction } from 'testcafe';
import jsQR from 'jsqr';
import { totp } from 'speakeasy';
import { v4 as uuid } from 'uuid';

import config from '../../config';
import {
  getLocation,
  loginWithInsightUser,
  signUpAndLogin,
  login,
} from '../../utils';
import { Sidebar } from '../../pages';

fixture('/login/verification').page(`${config.appBaseURL}/login/verification`);

test('Should be able to complete full flow', async (t) => {
  await t.expect(getLocation()).eql(`${config.appBaseURL}/login?dest=%2F`);
  await loginWithInsightUser(t);

  const password = uuid();
  const email = `loginverificationfullflow+${uuid()}@gmail.com`;
  await signUpAndLogin(t, {
    fullName: 'Miha Novak',
    company: 'Insight',
    email,
    password,
  });

  const codeInput = Selector('input[aria-label="Please enter your pin code"]');

  await t
    .hover(Sidebar.accountSettingsItem)
    .click(queryByText('Account settings'))
    .expect(getLocation())
    .eql(`${config.appBaseURL}/account/settings`)
    .click(queryByText('Two factor authentication'))
    .typeText(codeInput, '111111')
    .click(queryByText('Submit'))
    .expect(queryByText('Invalid code').visible)
    .ok('Should display invalid code error');

  const getQrImageData = ClientFunction(() => {
    const image = document.querySelector(
      'img[alt="TFA QR code"]'
    ) as HTMLImageElement;

    const canvas = document.createElement('canvas');
    const context = canvas.getContext('2d');

    canvas.width = image.width;
    canvas.height = image.height;
    context.drawImage(image, 0, 0);

    const imageData = context.getImageData(0, 0, image.width, image.height);
    return { data: imageData.data, width: image.width, height: image.height };
  });

  const imageData = await getQrImageData();
  const qrCode = jsQR(imageData.data, imageData.width, imageData.height);
  const tfaSecret = qrCode.data.split('?secret=')[1];

  await t
    .typeText(codeInput, totp({ secret: tfaSecret, encoding: 'base32' }))
    .expect(queryByText('Invalid code').value)
    .notOk('Should invalidate error on typing')
    .click(queryByText('Submit'))
    .expect(
      queryByText('Two factor authentication has been successfully set up')
        .visible
    )
    .ok('Should display positive message')
    .hover(Sidebar.accountSettingsItem)
    .click(queryByText('Sign out'));

  await login(t, { email, password })
    .expect(
      queryByText(
        'To protect your account, please complete the following verification.'
      ).visible
    )
    .ok('should display message')
    .expect(getLocation())
    .eql(`${config.appBaseURL}/login/verification?dest=%2F`)
    .typeText(codeInput, totp({ secret: tfaSecret, encoding: 'base32' }))
    .click(queryByText('Submit'))
    .hover(Sidebar.accountSettingsItem)
    .click(queryByText('Account settings'))
    .click(queryByText(/Two factor authentication.*/))
    .expect(
      queryByText('Two factor authentication has been successfully disabled')
        .visible
    )
    .ok('Should display message that TFA is disabled');
});
