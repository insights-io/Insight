import { within } from '@testing-library/testcafe';
import { Selector } from 'testcafe';

export class SettingsTopbar {
  private readonly container = Selector('nav.topbar.menu');
  private readonly withinContainer = within(this.container);

  public readonly list = this.container.find('ol');
  public readonly searchInput = this.withinContainer.queryByRole('combobox');

  public getNthPart = (n: number) => {
    return this.list.nth(n).child(0);
  };

  public getPartByText = (text: string) => {
    return within(this.list).queryByText(text);
  };
}

export default new SettingsTopbar();
