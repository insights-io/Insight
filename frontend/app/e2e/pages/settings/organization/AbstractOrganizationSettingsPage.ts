import { AbstractSettingsPage } from '../AbstractSettingsPage';
import SettingsTopbar from '../SettingsTopbar';

import OrganizationSettingsSidebar from './OrganizationSettingsSidebar';

export abstract class AbstractOrganizationSettingsPage extends AbstractSettingsPage {
  public readonly sidebar = OrganizationSettingsSidebar;
  public readonly topbar = SettingsTopbar;
}
