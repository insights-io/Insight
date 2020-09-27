import { AbstractSettingsPage } from '../AbstractSettingsPage';
import SettingsTopbar from '../SettingsTopbar';

import AccountSettingsSidebar from './AccountSettingsSidebar';

export abstract class AbstractAccountSettingsPage extends AbstractSettingsPage {
  public readonly sidebar = AccountSettingsSidebar;
  public readonly topbar = SettingsTopbar;
}
