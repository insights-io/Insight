import { Selector } from 'testcafe';
import { queryByText } from '@testing-library/testcafe';

import config from '../config';
import { loginWithInsightUser } from '../utils';

fixture('_app').page(config.appBaseURL);

test('Should be able to logout', async (t) => {
  await loginWithInsightUser(t);

  await t
    .hover(Selector('svg[id="account-settings"]'))
    .click(queryByText('Sign out'))
    .expect(queryByText('Create a free account').visible)
    .ok('Should be on the login page');
});
