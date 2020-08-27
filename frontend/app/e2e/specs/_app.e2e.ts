import { queryByText } from '@testing-library/testcafe';

import { LoginPage, SessionsPage, Sidebar, SignUpPage } from '../pages';
import config from '../config';
import { getLocation } from '../utils';

fixture('_app').page(config.appBaseURL);

test('Should be able to navigate', async (t) => {
  const { email, password } = SignUpPage.generateRandomCredentials();
  await SignUpPage.signUpAndLogin(t, { email, password });

  await t
    .hover(Sidebar.accountSettings.item)
    .expect(Sidebar.accountSettings.accountSettings.visible)
    .ok('Should display text on hover')
    .click(Sidebar.accountSettings.item)
    .click(Sidebar.accountSettings.signOut)
    .expect(LoginPage.createFreeAccount.visible)
    .ok('Should be on the login page');

  await LoginPage.login(t, { email, password })
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
    .eql(SessionsPage.path)
    .click(Sidebar.homeItem)
    .expect(getLocation())
    .eql(`${config.appBaseURL}/`);
});
