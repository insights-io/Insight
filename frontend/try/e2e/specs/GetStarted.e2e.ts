import { v4 as uuid } from 'uuid';
import { getByText, getByPlaceholderText } from '@testing-library/testcafe';
import { ClientFunction } from 'testcafe';

import config from '../config';

const getLocation = ClientFunction(() => window.location.href);

fixture('/').page(config.baseURL);

test('Can sign up', async (t) => {
  const emailInput = getByPlaceholderText('Email');
  const passwordInput = getByPlaceholderText('Password');
  const getStartedButton = getByText('Get started');

  await t
    .typeText(getByPlaceholderText('Full name'), 'Joe Makarena')
    .typeText(getByPlaceholderText('Company'), 'Insight')
    .typeText(emailInput, 'random')
    .typeText(passwordInput, 'short')
    .click(getStartedButton);

  await t
    .expect(getByText('Invalid email address').visible)
    .ok('Email input is validated')
    .expect(getByText('Password must be at least 8 characters long').visible)
    .ok('Password input is validated');

  await t
    .selectText(emailInput)
    .pressKey('delete')
    .typeText(emailInput, `ematej.snuderl+${uuid()}@gmail.com`)
    .selectText(passwordInput)
    .pressKey('delete')
    .typeText(passwordInput, `super_password_123`)
    .click(getStartedButton)
    .expect(
      getByText(
        'We have sent an email with a confirmation link to your email address.'
      ).visible
    )
    .ok('Should display message that email has been sent.')
    .expect(getLocation())
    .eql(
      `${config.baseURL}/signup-confirm`,
      'Should redirect to different page'
    );
});
