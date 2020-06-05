import {
  getByText,
  getByPlaceholderText,
  queryByText,
  getAllByText,
} from '@testing-library/testcafe';

import { getLocation } from '../utils';
import config from '../config';

fixture('/login').page(config.baseURL);

test('Login form should be validated both client & server side', async (t) => {
  const signInButton = getByText('Sign in');
  const emailInput = getByPlaceholderText('Email');
  const passwordInput = getByPlaceholderText('Password');
  const invalidEmailErrorMessage = 'Please enter a valid email address';
  const passwordTooShortErrorMessage =
    'Password must be at least 8 characters long';

  await t
    .expect(getLocation())
    .eql(`${config.baseURL}/login?dest=%2F`)
    .click(signInButton)
    .expect(getAllByText('Required').count)
    .eql(2, 'Both fields should be required')
    .typeText(emailInput, 'random')
    .typeText(passwordInput, 'short')
    .click(signInButton)
    .expect(getByText(invalidEmailErrorMessage).visible)
    .ok('Should display email validation error message')
    .expect(getByText(passwordTooShortErrorMessage).visible)
    .ok('Should display password validation error message');

  await t
    .selectText(emailInput)
    .pressKey('delete')
    .typeText(emailInput, 'random@gmail.com')
    .expect(queryByText(invalidEmailErrorMessage).exists)
    .notOk('Email validation error message should dissapear')
    .selectText(passwordInput)
    .pressKey('delete')
    .typeText(passwordInput, 'not_so_short')
    .expect(queryByText(passwordTooShortErrorMessage).exists)
    .notOk('Password validation error message should dissapear')
    .click(signInButton)
    .expect(getByText('Invalid email or password').visible)
    .ok('Should display email/password miss-match message');
});

test('Can navigate forth and back to /password/forgot', async (t) => {
  await t
    .click(getByText('Forgot?'))
    .expect(getLocation())
    .eql(`${config.baseURL}/password-forgot`)
    .click(getByText('Remember password?'))
    .expect(getLocation())
    .eql(`${config.baseURL}/login`);
});
