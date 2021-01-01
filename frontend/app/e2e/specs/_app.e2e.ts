import { LoginPage, SessionsPage, Sidebar, SignUpPage } from '../pages';
import config from '../config';
import { getLocation } from '../utils';

fixture('_app').page(config.appBaseURL);

test('User should be able to navigate around the app', async (t) => {
  const { email, password } = SignUpPage.generateRandomCredentials();
  await SignUpPage.signUpAndLogin(t, {
    email,
    password,
    company: 'My company',
    fullName: 'Marko skace',
  });

  await t
    .click(Sidebar.banner.trigger)
    .expect(Sidebar.banner.menu.organization.cardTitle.innerText)
    .eql('My company', 'Matches company name')
    .expect(Sidebar.banner.menu.organization.cardSubtitle.innerText)
    .eql('Marko skace', 'Matches full name')
    .expect(Sidebar.banner.menu.account.cardTitle.innerText)
    .eql('Marko skace', 'Matches full name')
    .expect(Sidebar.banner.menu.account.cardSubtitle.innerText)
    .eql(email, 'Matches email')
    .click(Sidebar.banner.menu.account.signOut)
    .expect(LoginPage.createFreeAccount.visible)
    .ok('Should be on the login page');

  await LoginPage.login(t, { email, password })
    .hover(Sidebar.toggleItem)
    .expect(Sidebar.toggleExpandTooltip.visible)
    .ok('Expand sidebar text should exist')
    .click(Sidebar.toggleItem)
    .expect(Sidebar.toggleCollapseTooltip.visible)
    .ok('Should display text of the toggle sidebar item')
    .click(Sidebar.sessionsItem)
    .expect(Sidebar.toggleCollapseTooltip.visible)
    .notOk('Collapse text should not be vissible due to sidebar collapsing')
    .expect(getLocation())
    .eql(SessionsPage.path)
    .click(Sidebar.homeItem)
    .expect(getLocation())
    .eql(`${config.appBaseURL}/`);
});
