import { getByText, getByPlaceholderText } from '@testing-library/testcafe';
import { v4 as uuid } from 'uuid';

import config from '../config';

const page = '/password-forgot';

fixture(page).page(`${config.baseURL}${page}`);

test('Password forgot form should be validated both client & server side & go through even on random emails', async (t) => {
  const emailInput = getByPlaceholderText('Email');
  const submitButton = getByText('Reset password');

  await t
    .typeText(emailInput, 'short')
    .click(submitButton)
    .expect(getByText('Please enter a valid email address').visible)
    .ok('Should validate email input')
    .selectText(emailInput)
    .pressKey('delete')
    .typeText(emailInput, `not-so-short+${uuid()}@gmail.com`)
    .click(submitButton)
    .expect(
      getByText(
        'If your email address is associated with an Insight account, you will be receiving a password reset request shortly.'
      ).visible
    )
    .ok('Should display nice message that email has been sent');
});
