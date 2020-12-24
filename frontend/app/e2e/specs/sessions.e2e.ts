/* eslint-disable no-console */
import { queryByText, queryAllByText } from '@testing-library/testcafe';

import { LoginPage, SessionPage, SessionsPage } from '../pages';

fixture('/sessions').page(SessionsPage.path);

test('User should be able to see page sessions', async (t) => {
  await LoginPage.loginWithRebrowseUser(t)
    .click(SessionsPage.getLastSession())
    .click(SessionPage.devtools.button)
    .expect(SessionPage.devtools.filterInput.visible)
    .ok('Navigates to session details page');

  await t.eval(() => {
    console.log('console.log');
    console.info('console.info');
    console.debug('console.debug');
    console.warn('console.warn');
    console.error('console.error');

    // eslint-disable-next-line no-restricted-globals
    location.reload(true);
  });

  await t
    .click(SessionPage.devtools.button)
    .typeText(SessionPage.devtools.filterInput, 'console')
    .expect(queryByText('console.log').visible)
    .ok('console.log should be visible in the console', { timeout: 10000 }) // this might take some time -- refresh interval is 5s
    .expect(queryByText('console.info').visible)
    .ok('console.info should be visible in the console')
    .expect(queryByText('console.debug').visible)
    .ok('console.debug should be visible in the console')
    .expect(queryByText('console.warn').visible)
    .ok('console.warn should be visible in the console')
    .expect(queryByText('console.error').visible)
    .ok('console.error should be visible in the console');

  await t
    .click(SessionPage.devtools.tabs.network)
    .expect(queryAllByText('POST').visible)
    .ok('Multiple POST requests')
    .expect(queryAllByText('GET').visible)
    .ok('Multiple GET requests')
    .expect(queryAllByText('sessions').visible)
    .ok('Should display GET /sessions request')
    .expect(queryAllByText('login').visible)
    .ok('Should display /login request')
    .expect(
      queryAllByText(`search?event.e=gte:9&event.e=lte:10&limit=1000`).visible
    )
    .ok('Should display console events search request');
});
