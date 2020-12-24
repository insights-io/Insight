/* eslint-disable no-console */
import { queryByText, queryAllByText } from '@testing-library/testcafe';

import { LoginPage, SessionPage, SessionsPage } from '../pages';
import { PageVisitInterceptor } from '../utils/PageVisitInterceptor';

fixture('/sessions').page(SessionsPage.path);

const pageVisitInterceptor = new PageVisitInterceptor();

test.requestHooks(pageVisitInterceptor)(
  'As a user I should be able to use "Developer tools" to investigate problems',
  async (t) => {
    await LoginPage.loginWithRebrowseUser(t);

    /* Generate some events for Developer tools */
    await t.eval(() => {
      console.log('console.log');
      console.info('console.info');
      console.debug('console.debug');
      console.warn('console.warn');
      console.error('console.error');

      // simulate Error thrown in browser (we cant just throw here as this will kill the process)
      const errorElem = document.createElement('script');
      errorElem.textContent = 'throw new Error("simulated error");';
      document.body.append(errorElem);

      // simulate SyntaxError thrown in browser (we cant just throw here as this will kill the process)
      const syntaxErrorElem = document.createElement('script');
      syntaxErrorElem.textContent = 'eval("va x = 5;");';
      document.body.append(syntaxErrorElem);

      // eslint-disable-next-line no-restricted-globals
      location.reload(true);
    });

    const sessionId = pageVisitInterceptor.getSessionId();
    const sessionPage = new SessionPage(sessionId);

    await t
      .click(SessionsPage.getItemBySessionId(sessionId))
      .expect(queryByText(sessionId).visible)
      .ok('Session ID is visible')
      .expect(queryByText('Unknown location').visible)
      .ok('Unknown location')
      .expect(queryByText('browser.name = Chrome').visible)
      .ok('Is in Chrome')
      .click(sessionPage.backButton)
      .click(SessionsPage.getItemBySessionId(sessionId));

    await t
      .click(sessionPage.devtools.button)
      .typeText(sessionPage.devtools.filterInput, 'console')
      .expect(queryByText('console.log').visible)
      .ok('console.log should be visible in the console', { timeout: 15000 })
      .expect(queryByText('console.info').visible)
      .ok('console.info should be visible in the console')
      .expect(queryByText('console.debug').visible)
      .ok('console.debug should be visible in the console')
      .expect(queryByText('console.warn').visible)
      .ok('console.warn should be visible in the console')
      .expect(queryByText('console.error').visible)
      .ok('console.error should be visible in the console')
      .selectText(sessionPage.devtools.filterInput)
      .pressKey('delete')
      .typeText(sessionPage.devtools.filterInput, 'Error')
      .expect(queryByText('Uncaught Error: simulated error').visible)
      .ok('Uncaught Error should be visible in console');

    await t
      .click(sessionPage.devtools.tabs.network)
      .expect(queryAllByText('POST').visible)
      .ok('POST requests')
      .expect(queryAllByText('GET').visible)
      .ok('GET requests')
      .expect(queryAllByText('user').visible)
      .ok('GET /user request')
      .expect(queryAllByText('organization').visible)
      .ok('GET /organization request')
      .expect(
        queryAllByText(`search?event.e=gte:9&event.e=lte:10&limit=1000`).visible
      )
      .ok('Should display console events search request', { timeout: 15000 });
  }
);
