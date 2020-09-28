import { within } from '@testing-library/testcafe';
import { Selector } from 'testcafe';

export abstract class AbstractSettingsSidebar {
  protected readonly container = Selector('nav.sidebar.menu');
  protected readonly withinContainer = within(this.container);
}
