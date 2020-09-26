import { queryByText } from '@testing-library/testcafe';
import { Selector } from 'testcafe';

class Sidebar {
  private readonly container = Selector('nav');

  public readonly homeItem = this.container
    .find('a')
    .withAttribute('href', '/')
    .child();

  public readonly sessionsItem = this.container
    .find('a')
    .withAttribute('href', '/sessions')
    .child();

  public readonly toggleItem = this.container
    .find('svg')
    .withAttribute('id', 'sidebar--togle')
    .parent();

  public readonly toggleExpandTooltip = queryByText('Expand');
  public readonly toggleCollapseTooltip = queryByText('Collapse');

  public readonly accountTab = {
    trigger: this.container.find('svg').withAttribute('id', 'account').parent(),
    tooltip: queryByText('Account'),

    menu: {
      settings: queryByText('Settings'),
      signOut: queryByText('Sign out'),
    },
  };

  public signOut = (t: TestController) => {
    return t
      .click(this.accountTab.trigger)
      .click(this.accountTab.menu.settings);
  };
}

export default new Sidebar();
