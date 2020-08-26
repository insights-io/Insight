import { queryByText } from '@testing-library/testcafe';
import { Selector } from 'testcafe';

class Sidebar {
  private readonly container = Selector('nav', { timeout: 1000 });

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

  public readonly accountSettings = {
    item: this.container
      .find('svg')
      .withAttribute('id', 'account-settings')
      .parent(),

    accountSettings: queryByText('Account settings'),
    signOut: queryByText('Sign out'),
  };

  public signOut = (t: TestController) => {
    return t
      .click(this.accountSettings.item)
      .click(this.accountSettings.signOut);
  };
}

export default new Sidebar();
