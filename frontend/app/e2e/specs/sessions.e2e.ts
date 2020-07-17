/* eslint-disable no-console */
import {
  queryByText,
  queryByPlaceholderText,
  getByText,
  queryAllByText,
} from '@testing-library/testcafe';
import { Selector } from 'testcafe';

import { login } from '../utils';
import config from '../config';

fixture('/sessions').page(`${config.appBaseURL}/login`);

test('Should be able to see sessions for Insight logged in user', async (t) => {
  await login(t, {
    email: config.insightUserEmail,
    password: config.insightUserPassword,
  });

  const lastSession = queryByText('less than 5 seconds ago');
  const lastSessionListItem = lastSession.parent().parent().parent().parent();
  const showDevToolsIcon = Selector('svg[title="Arrow Left"]');

  await t
    .expect(lastSession.visible)
    .ok('Newly created session is dispalyed')
    .click(lastSessionListItem)
    .click(showDevToolsIcon)
    .expect(queryByPlaceholderText('Filter').visible)
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
    .click(showDevToolsIcon)
    .expect(queryByText('console.log').visible)
    .ok('console.log should be visible in the console')
    .expect(queryByText('console.info').visible)
    .ok('console.info should be visible in the console')
    .expect(queryByText('console.debug').visible)
    .ok('console.debug should be visible in the console')
    .expect(queryByText('console.warn').visible)
    .ok('console.warn should be visible in the console')
    .expect(queryByText('console.error').visible)
    .ok('console.error should be visible in the console');

  await t

    .click(getByText('Network'))
    .expect(queryAllByText('POST').visible)
    .ok('Multiple POST requests')
    .expect(queryAllByText('GET').visible)
    .ok('Multiple GET requests')
    .expect(queryAllByText('sessions').visible)
    .ok('Should display GET /sessions request')
    .expect(queryAllByText('login').visible)
    .ok('Should display /login request')
    .expect(
      queryAllByText('search?event.e=gte:9&event.e=lte:10&limit=1000').visible
    )
    .ok('Should display console events search request');
});
