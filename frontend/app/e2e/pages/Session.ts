import {
  queryByLabelText,
  queryByPlaceholderText,
  queryByText,
} from '@testing-library/testcafe';

import { SESSIONS_PAGE } from '../../src/shared/constants/routes';
import config from '../config';
import { getLocation } from '../utils';

class SessionPage {
  /* Selectors */

  public readonly devtools = {
    button: queryByLabelText('Developer tools'),
    filterInput: queryByPlaceholderText('Filter'),

    tabs: {
      console: queryByText('Console'),
      network: queryByText('Network'),
    },
  };

  /* Utils */
  public path = () => {
    return this.getId().then(
      (id) => `${config.appBaseURL}${SESSIONS_PAGE}/${id}`
    );
  };

  public getId = () => {
    return getLocation().then((path) => {
      const split = path.split('/');
      return split[split.length - 1];
    });
  };
}

export default new SessionPage();
