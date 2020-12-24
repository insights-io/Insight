import { SESSIONS_PAGE } from '../../src/shared/constants/routes';

import { AbstractPage } from './AbstractPage';

class SessionsPage extends AbstractPage {
  public readonly sessionsList = this.container.find('.sessions ul');
  public readonly firstSession = this.sessionsList.child(0);
}

export default new SessionsPage(SESSIONS_PAGE);
