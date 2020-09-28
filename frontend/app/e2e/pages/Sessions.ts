import { queryAllByText } from '@testing-library/testcafe';

import { SESSIONS_PAGE } from '../../src/shared/constants/routes';

import { AbstractPage } from './AbstractPage';

class SessionsPage extends AbstractPage {
  /* Utils */
  public getLastSession = () => {
    return queryAllByText(/^.*less than [1-9][0-9]* seconds ago$/);
  };
}

export default new SessionsPage(SESSIONS_PAGE);
