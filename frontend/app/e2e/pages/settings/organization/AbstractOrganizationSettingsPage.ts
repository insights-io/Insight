import { AbstractPage } from '../../AbstractPage';
import SettingsTopbar from '../SettingsTopbar';

import OrganizationSettingsSidebar from './OrganizationSettingsSidebar';

export abstract class AbstractOrganizationSettingsPage extends AbstractPage {
  public readonly sidebar = OrganizationSettingsSidebar;
  public readonly topbar = SettingsTopbar;
}
