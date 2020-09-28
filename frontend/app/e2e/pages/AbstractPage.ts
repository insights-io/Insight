import { within } from '@testing-library/testcafe';
import { Selector } from 'testcafe';

import config from '../config';

export abstract class AbstractPage {
  public readonly pathname: string;
  public readonly path: string;

  constructor(pathname: string) {
    this.pathname = pathname;
    this.path = `${config.appBaseURL}${pathname}`;
  }

  protected readonly container = Selector('main');
  protected readonly withinContainer = within(this.container);
}
