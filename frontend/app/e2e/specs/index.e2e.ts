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
  const initialSessionsCount = await HomePage.getSessionsCount().then(Number);

  await t
    .expect(initialPageVisitsCount)
    .gt(0, 'Page visit count should be greater than 0')
    .expect(initialSessionsCount)
    .gt(0, 'Sessions count should be greater than 0');

  // Just reloading the page happens too quickly and can be flaky
  // Navigate somewhere else, reload there and navigate back to / to make some
  // more time for page visit to be created
  // TODO: should be solved with websockets?
  await t.navigateTo('/sessions');
  await t.eval(() => {
    // eslint-disable-next-line no-restricted-globals
    location.reload(true);
  });
  await t.navigateTo('/');

  const newPageVisitCount = await HomePage.getPageVisitsCount().then(Number);
  await t
    .expect(newPageVisitCount)
    .gt(
      initialPageVisitsCount,
      'Page visit count should be greater than initially after page reload'
    );
});
