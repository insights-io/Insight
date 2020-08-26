import { queryByPlaceholderText } from '@testing-library/testcafe';
import { Selector } from 'testcafe';

import config from '../config';
import { getLocation } from '../utils';

class SessionPage {
  /* Selectors */

  public readonly devtools = {
    button: Selector('svg[id="devtools"]'),
    filterInput: queryByPlaceholderText('Filter'),
  };

  /* Utils */
  public path = () => {
    return this.getId().then((id) => `${config.appBaseURL}/sessions/${id}`);
  };

  public getId = () => {
    return getLocation().then((path) => {
      const split = path.split('/');
      return split[split.length - 1];
    });
  };
}

export default new SessionPage();
