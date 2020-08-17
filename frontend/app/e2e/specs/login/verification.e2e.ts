import { queryByText, getByText } from '@testing-library/testcafe';
import { Selector } from 'testcafe';
import jsQR from 'jsqr';
import { totp } from 'speakeasy';
import { v4 as uuid } from 'uuid';

import config from '../../config';
import {
  getLocation,
  passwordInput,
  signUpAndLogin,
  login,
  forgotPasswordButton,
  emailInput,
  startPasswordResetButton,
  findLinkFromDockerLog,
  finishPasswordResetButton,
  totpLogin,
} from '../../utils';
import { Sidebar } from '../../pages';
import { getQrImageData } from '../../utils/io';

fixture('/login/verification').page(`${config.appBaseURL}/login/verification`);

const codeInput = Selector('input[aria-label="Please enter your pin code"]');
const emailSentMessage = getByText(
  'If your email address is associated with an Insight account, you will be receiving a password reset request shortly.'
);

test('Should be able to complete full TFA flow after password reset', async (t) => {
  await t.expect(getLocation()).eql(`${config.appBaseURL}/login?dest=%2F`);

  const password = uuid();
  const email = `${uuid()}@gmail.com`;
  await signUpAndLogin(t, {
    fullName: 'Miha Novak',
    company: 'Insight',
    email,
    password,
  });

  await t
    .hover(Sidebar.accountSettingsItem)
    .expect(queryByText('Account settings').visible)
    .ok('Should display text on hover')
    .click(Sidebar.accountSettingsItem)
    .click(queryByText('Account settings'))
    .expect(getLocation())
    .eql(`${config.appBaseURL}/account/settings`)
    .click(queryByText('Two factor authentication'))
    .typeText(codeInput, '111111')
    .click(queryByText('Submit'))
    .expect(queryByText('Invalid code').visible)
    .ok('Should display invalid code error');

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
    .expect(queryByText('Account settings').visible)
    .ok('Should display text on hover')
    .click(Sidebar.accountSettingsItem)
    .click(queryByText('Sign out'))
    .click(forgotPasswordButton)
    .typeText(emailInput, email)
    .click(startPasswordResetButton)
    .expect(emailSentMessage.visible)
    .ok('Should display nice message that email has been sent');

  const passwordResetLink = findLinkFromDockerLog();
  const newPassword = uuid();
  await t
    .navigateTo(passwordResetLink)
    .expect(passwordInput.visible)
    .ok('Password input is visible')
    .typeText(passwordInput, newPassword)
    .click(finishPasswordResetButton)
    .expect(getLocation())
    .eql(`${config.appBaseURL}/login/verification?dest=%2F`);

  await totpLogin(t, tfaSecret, codeInput);
  await t
    .hover(Sidebar.accountSettingsItem)
    .expect(queryByText('Account settings').visible)
    .ok('Should display text on hover')
    .click(Sidebar.accountSettingsItem)
    .click(queryByText('Account settings'))
    .click(queryByText(/Two factor authentication.*/))
    .expect(
      queryByText('Two factor authentication has been successfully disabled')
        .visible
    )
    .ok('Should display message that TFA is disabled');
});

test('Should be able to complete full TFA flow', async (t) => {
  await t.expect(getLocation()).eql(`${config.appBaseURL}/login?dest=%2F`);

  const password = uuid();
  const email = `${uuid()}@gmail.com`;
  await signUpAndLogin(t, {
    fullName: 'Miha Novak',
    company: 'Insight',
    email,
    password,
  });

  await t
    .hover(Sidebar.accountSettingsItem)
    .expect(queryByText('Account settings').visible)
    .ok('Should display text on hover')
    .click(Sidebar.accountSettingsItem)
    .click(queryByText('Account settings'))
    .expect(getLocation())
    .eql(`${config.appBaseURL}/account/settings`)
    .click(queryByText('Two factor authentication'))
    .typeText(codeInput, '111111')
    .click(queryByText('Submit'))
    .expect(queryByText('Invalid code').visible)
    .ok('Should display invalid code error');

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
    .expect(queryByText('Account settings').visible)
    .ok('Should display text on hover')
    .click(Sidebar.accountSettingsItem)
    .click(queryByText('Sign out'));

  await login(t, { email, password })
    .expect(
      queryByText(
        'To protect your account, please complete the following verification.'
      ).visible
    )
    .ok('should display message')
    .expect(getLocation())
    .eql(`${config.appBaseURL}/login/verification?dest=%2F`);

  await totpLogin(t, tfaSecret, codeInput);
  await t
    .hover(Sidebar.accountSettingsItem)
    .expect(queryByText('Account settings').visible)
    .ok('Should display text on hover')
    .click(Sidebar.accountSettingsItem)
    .click(queryByText('Account settings'))
    .click(queryByText(/Two factor authentication.*/))
    .expect(
      queryByText('Two factor authentication has been successfully disabled')
        .visible
    )
    .ok('Should display message that TFA is disabled');
});
