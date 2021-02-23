import { INDEX_ROUTE } from 'shared/constants/routes';

import * as SignInPageSetup from './signin/SignInPageSetup';

describe('/', () => {
  test('As a user visiting / I should be redirected to /signin', async () => {
    await SignInPageSetup.setup(INDEX_ROUTE);
    SignInPageSetup.getElements();
  });
});
