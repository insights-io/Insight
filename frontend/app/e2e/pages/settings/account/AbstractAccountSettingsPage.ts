import { within } from '@testing-library/testcafe';
import { Selector } from 'testcafe';

import SettingsTopbar from '../SettingsTopbar';

import AccountSettingsSidebar from './AccountSettingsSidebar';

export abstract class AbstractAccountSettingsPage {
  public readonly sidebar = AccountSettingsSidebar;
  public readonly topbar = SettingsTopbar;

  protected readonly container = within(Selector('main'));
}
