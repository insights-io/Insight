import { within } from '@testing-library/testcafe';
import { Selector } from 'testcafe';

import SettingsTopbar from '../SettingsTopbar';

import OrganizationSettingsSidebar from './OrganizationSettingsSidebar';

export abstract class AbstractOrganizationSettingsPage {
  public readonly sidebar = OrganizationSettingsSidebar;
  public readonly topbar = SettingsTopbar;

  protected readonly containerSelector = Selector('main');
  protected readonly container = within(this.containerSelector);
}
