import { SESSIONS_PAGE } from '../../src/shared/constants/routes';

import { AbstractPage } from './AbstractPage';

class SessionsPage extends AbstractPage {
  public readonly sessionsList = this.container.find('div.sessions').find('ul');

  public readonly getItemBySessionId = (id: string) => {
    return this.sessionsList
      .find('a')
      .withAttribute('href', `${SESSIONS_PAGE}/${id}`)
      .child();
  };
}

export default new SessionsPage(SESSIONS_PAGE);
