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

  public readonly accountSettings = {
    item: this.container
      .find('svg')
      .withAttribute('id', 'account-settings')
      .parent(),

    accountSettings: queryByText('Account settings'),
    signOut: queryByText('Sign out'),
  };
}

export default new Sidebar();
