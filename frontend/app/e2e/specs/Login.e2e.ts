import {
  getByText,
  getByPlaceholderText,
  queryByText,
  getAllByText,
} from '@testing-library/testcafe';

import config from '../config';

fixture('<IndexPage />').page(config.baseURL);

test('Login form should be validated both client & server side', async (t) => {
  const signInButton = getByText('Sign in');
  const emailInput = getByPlaceholderText('Email');
  const passwordInput = getByPlaceholderText('Password');
  const invalidEmailErrorMessage = 'Please enter a valid email address';
  const passwordTooShortErrorMessage =
    'Password must be at least 8 characters long';

  await t
    .click(signInButton)
    .expect(getAllByText('Required').count)
    .eql(2, 'Both fields should be required')
    .typeText(emailInput, 'random')
    .typeText(passwordInput, 'short')
    .click(signInButton)
    .expect(getByText(invalidEmailErrorMessage).visible)
    .ok('Should display email validation error message')
    .expect(getByText(passwordTooShortErrorMessage).visible)
    .ok('Should display password validation error message')
    .typeText(emailInput, '@gmail.com')
    .expect(queryByText(invalidEmailErrorMessage).exists)
    .notOk('Email validation error message should dissapear')
    .typeText(passwordInput, 'long')
    .expect(queryByText(passwordTooShortErrorMessage).exists)
    .notOk('Password validation error message should dissapear');
});
