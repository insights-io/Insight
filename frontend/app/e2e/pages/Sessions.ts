import { queryAllByText } from '@testing-library/testcafe';

import config from '../config';

class SessionsPage {
  public readonly path = `${config.appBaseURL}/sessions`;

  /* Utils */
  public getLastSession = () => {
    return queryAllByText(/^.*less than 5 seconds ago$/);
  };
}

export default new SessionsPage();
