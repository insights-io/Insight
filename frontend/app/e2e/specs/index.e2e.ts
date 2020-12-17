import { HomePage, LoginPage } from '../pages';

fixture('/').page(HomePage.path);

test('User should be able to see page sessions', async (t) => {
  await LoginPage.loginWithRebrowseUser(t)
    .expect(HomePage.pageVisitsCardText.visible)
    .ok('Page visits card visible')
    .expect(HomePage.sessionsCardText.visible)
    .ok('Sessions card visible')
    .expect(HomePage.getPageVisitsSum().then(Number))
    .gt(0, 'Page visit count should be greated than 0')
    .expect(HomePage.getSessionsSum().then(Number))
    .gt(0, 'Sessions count should be greated than 0');
});
