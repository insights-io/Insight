/* eslint-disable no-console */
import {
  getByText,
  queryByText,
  getByPlaceholderText,
} from '@testing-library/testcafe';

import { login } from '../utils';
import config from '../config';

fixture('/sessions').page(`${config.appBaseURL}/login `);

test('Should be able to see sessions for Insight logged in user', async (t) => {
  await login(t, {
    email: config.insightUserEmail,
    password: config.insightUserPassword,
  });

  const lastSession = getByText('less than 5 seconds ago');
  const lastSessionListItem = lastSession.parent().parent().parent().parent();

  await t
    .expect(lastSession.visible)
    .ok('Newly created session is dispalyed')
    .click(lastSessionListItem);

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
    .typeText(getByPlaceholderText('Filter'), 'console')
    .expect(queryByText('console.log').visible)
    .ok('console.log should be visible in the console', { timeout: 60000 })
    .expect(queryByText('console.info').visible)
    .ok('console.info should be visible in the console', { timeout: 60000 })
    .expect(queryByText('console.debug').visible)
    .ok('console.debug should be visible in the console', { timeout: 60000 })
    .expect(queryByText('console.warn').visible)
    .ok('console.warn should be visible in the console', { timeout: 60000 })
    .expect(queryByText('console.error').visible)
    .ok('console.error should be visible in the console', { timeout: 60000 });
});
