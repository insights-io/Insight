import { queryByText } from '@testing-library/testcafe';

import { Sidebar } from '../pages';
import config from '../config';
import { loginWithInsightUser, getLocation } from '../utils';

fixture('_app').page(config.appBaseURL);

test('Should be able to navigate', async (t) => {
  await loginWithInsightUser(t);

  await t
    .hover(Sidebar.accountSettingsItem)
    .click(queryByText('Sign out'))
    .expect(queryByText('Create a free account').visible)
    .ok('Should be on the login page');

  await loginWithInsightUser(t);

  await t
    .hover(Sidebar.toggleItem)
    .expect(queryByText('Expand').visible)
    .ok('Expand sidebar text should exist')
    .click(Sidebar.toggleItem)
    .expect(queryByText('Collapse').visible)
    .ok('Should display text of the toggle sidebar item')
    .click(Sidebar.sessionsItem)
    .expect(queryByText('Collapse').visible)
    .notOk('Collapse text should not be vissible due to sidebar collapsing')
    .expect(getLocation())
    .eql(`${config.appBaseURL}/sessions`)
    .click(Sidebar.homeItem)
    .expect(getLocation())
    .eql(config.appBaseURL);
});
