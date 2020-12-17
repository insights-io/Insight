import { INDEX_PAGE } from '../../src/shared/constants/routes';

import { AbstractPage } from './AbstractPage';

class HomePage extends AbstractPage {
  public readonly pageVisitsCardText = this.withinContainer.queryByText(
    'Page Visits'
  );

  public readonly sessionsCardText = this.withinContainer.queryByText(
    'Sessions'
  );

  public getPageVisitsSum = () => {
    return this.container.find('p.stat--sum').innerText;
  };

  public getSessionsSum = () => {
    return this.container.find('p.stat--sum').innerText;
  };
}

export default new HomePage(INDEX_PAGE);
