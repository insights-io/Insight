import 'testcafe';
import { queryAllByText } from '@testing-library/testcafe';

import config from '../config';

class SessionsPage {
  public readonly path = `${config.appBaseURL}/sessions`;

  /* Utils */
  public getLastSession = () => {
    return queryAllByText(/^.*less than [1-9][0-9]* seconds ago$/);
  };
}

export default new SessionsPage();
