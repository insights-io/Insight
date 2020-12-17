import { INDEX_PAGE } from '../../src/shared/constants/routes';

import { AbstractPage } from './AbstractPage';

class HomePage extends AbstractPage {
  public readonly pageVisitsCardText = this.withinContainer.queryByText(
    'Page Visits'
  );

  public readonly sessionsCardText = this.withinContainer.queryByText(
    'Sessions'
  );

  public getPageVisitsCount = () => {
    return this.pageVisitsCardText.parent().parent().find('p.stat--sum')
      .innerText;
  };

  public getSessionsCount = () => {
    return this.sessionsCardText.parent().parent().find('p.stat--sum')
      .innerText;
  };
}

export default new HomePage(INDEX_PAGE);
