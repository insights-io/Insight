import { getByText } from '@testing-library/testcafe';
import { v4 as uuid } from 'uuid';
import { Selector } from 'testcafe';

import config from '../config';
import {
  emailInput,
  signUpAndLogin,
  findLinkFromDockerLog,
  passwordInput,
  finishPasswordResetButton,
  startPasswordResetButton,
  forgotPasswordButton,
} from '../utils';

const page = '/password-forgot';
const emailSentMessage = getByText(
  'If your email address is associated with an Insight account, you will be receiving a password reset request shortly.'
);

fixture(page).page(`${config.appBaseURL}`);

test('User should be able to reset password', async (t) => {
  const password = uuid();
  const email = `miha.novak+${uuid()}@gmail.com`;

  await signUpAndLogin(t, {
    fullName: 'Miha Novak',
    company: 'Insight',
    email,
    password,
  });

  await t
    .click(Selector('svg[title="Menu"]'))
    .click(getByText('Sign out'))
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
    .expect(getByText('THIS IS WHERE THE MAGIC WILL HAPPEN').visible)
    .ok('Should be signed in to app');
});

test('User should be able to enter any email into apssword reset form and see message aboyut email being sent', async (t) => {
  await t
    .navigateTo(page)
    .typeText(emailInput, 'short')
    .click(startPasswordResetButton)
    .expect(getByText('Please enter a valid email address').visible)
    .ok('Should validate email input')
    .selectText(emailInput)
    .pressKey('delete')
    .typeText(emailInput, `not-so-short+${uuid()}@gmail.com`)
    .click(startPasswordResetButton)
    .expect(emailSentMessage.visible)
    .ok('Should display nice message that email has been sent');
});
