import {
  queryByLabelText,
  queryByPlaceholderText,
  queryByText,
} from '@testing-library/testcafe';

import { SESSIONS_PAGE } from '../../src/shared/constants/routes';

import { AbstractPage } from './AbstractPage';

export class SessionPage extends AbstractPage {
  private readonly id: string;

  constructor(id: string) {
    super(`${SESSIONS_PAGE}/${id}`);
    this.id = id;
  }

  public readonly backButton = this.withinContainer.queryByLabelText(
    'Back to all sesions'
  );

  public readonly devtools = {
    button: queryByLabelText('Developer tools'),
    filterInput: queryByPlaceholderText('Filter'),

    tabs: {
      console: queryByText('Console'),
      network: queryByText('Network'),
    },
  };
}
