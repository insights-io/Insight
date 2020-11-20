import React from 'react';
import { render } from 'test/utils';
import { sandbox } from '@rebrowse/testing';
import userEvent from '@testing-library/user-event';

import { Base } from './SessionPage.stories';

describe('<SessionPage />', () => {
  it('Should render log events in the console', async () => {
    Base.story.setupMocks(sandbox);
    const { findByText, queryByText, getByPlaceholderText, container } = render(
      <Base />
    );

    const toggleDevToolsIcon = container.querySelector(
      'svg[id="devtools"]'
    ) as SVGElement;

    userEvent.click(toggleDevToolsIcon);

    await findByText('[Fast Refresh] done');

    await userEvent.type(getByPlaceholderText('Filter'), 'but codemods are');
    expect(queryByText('[Fast Refresh] done')).toBeNull();

    await findByText(
      `Vendors~main.6e71f8501d51c505cf1d.bundle.js:70044 The default hierarchy separators are changing in Storybook 6.0`,
      { exact: false }
    );
  });
});
