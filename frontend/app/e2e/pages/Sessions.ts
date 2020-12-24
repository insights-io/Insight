import { RequestLogger } from 'testcafe';

import { SESSIONS_PAGE } from '../../src/shared/constants/routes';

import { AbstractPage } from './AbstractPage';

export const createPageVisitLogger = () => {
  const pageVisitLogger = RequestLogger((request: Request) => {
    return request.url.endsWith('/v1/pages') && request.method === 'post';
  });

  let sessionId: string | undefined;

  pageVisitLogger.onResponse(async (response: Response) => {
    const dataResponse = await response.json();
    sessionId = dataResponse.data.sessionId;
  });

  return { logger: pageVisitLogger, getSessionId: () => sessionId };
};

class SessionsPage extends AbstractPage {
  public readonly sessionsList = this.container.find('.sessions').find('ul');
}

export default new SessionsPage(SESSIONS_PAGE);
