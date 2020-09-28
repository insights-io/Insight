import { AbstractPage } from '../../AbstractPage';
import SettingsTopbar from '../SettingsTopbar';

import AccountSettingsSidebar from './AccountSettingsSidebar';

export abstract class AbstractAccountSettingsPage extends AbstractPage {
  public readonly sidebar = AccountSettingsSidebar;
  public readonly topbar = SettingsTopbar;
}
