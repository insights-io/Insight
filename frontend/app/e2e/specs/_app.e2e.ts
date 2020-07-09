import { Selector } from 'testcafe';
import { queryByText } from '@testing-library/testcafe';

import config from '../config';
import { login } from '../utils';

fixture('_app').page(config.appBaseURL);

test('Should be able to logout', async (t) => {
  await login(t, {
    email: config.insightUserEmail,
    password: config.insightUserPassword,
  });

  await t
    .click(Selector('svg[title="Menu"]'))
    .click(queryByText('Sign out'))
    .expect(queryByText('Create a free account').visible)
    .ok('Should be on the login page');
});
