import { HomePage, LoginPage } from '../pages';

fixture('/').page(HomePage.path);

test('As a user I should see live data on page visits & sessions', async (t) => {
  await LoginPage.loginWithRebrowseUser(t)
    .expect(HomePage.pageVisitsCardText.visible)
    .ok('Page visits card visible')
    .expect(HomePage.sessionsCardText.visible)
    .ok('Sessions card visible');

  const initialPageVisitsCount = await HomePage.getPageVisitsCount().then(
    Number
  );
  const initialSessionsCount = HomePage.getSessionsCount().then(Number);

  await t
    .expect(initialPageVisitsCount)
    .gt(0, 'Page visit count should be greater than 0')
    .expect(initialSessionsCount)
    .gt(0, 'Sessions count should be greater than 0');

  await t.eval(() => {
    // eslint-disable-next-line no-restricted-globals
    location.reload(true);
  });

  await t
    .expect(HomePage.getPageVisitsCount().then(Number))
    .gt(
      initialPageVisitsCount,
      'Page visit count should be greater than initially after page reload'
    );
});
