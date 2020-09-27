import { within } from '@testing-library/testcafe';
import { Selector } from 'testcafe';

import config from '../../config';

export abstract class AbstractSettingsPage {
  public readonly relativePath: string;
  public readonly path: string;

  constructor(relativePath: string) {
    this.relativePath = relativePath;
    this.path = `${config.appBaseURL}${relativePath}`;
  }

  protected readonly containerSelector = Selector('main');
  protected readonly container = within(this.containerSelector);
}
