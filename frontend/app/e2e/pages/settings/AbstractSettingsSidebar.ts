import { within } from '@testing-library/testcafe';
import { Selector } from 'testcafe';

export abstract class AbstractSettingsSidebar {
  protected readonly container = within(Selector('nav.sidebar.menu'));
}
