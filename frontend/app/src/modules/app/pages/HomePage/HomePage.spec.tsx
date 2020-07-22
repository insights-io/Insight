import React from 'react';
import { render } from 'test/utils';
import userEvent from '@testing-library/user-event';
import { sandbox } from '@insight/testing';
import { INSIGHT_SESSION } from 'test/data';

import { NoSessions, WithSessions } from './HomePage.stories';

describe('<HomePage />', () => {
  it('Should render recording snippet on no sessions', async () => {
    const { findByText } = render(<NoSessions />);

    await findByText("s._i_org = '000000';", { exact: false });
    await findByText("n.src = 'https://static.dev.snuderls.eu/s/insight.js';", {
      exact: false,
    });
  });

  it('Should render session list when sessions', () => {
    const { getByText, queryByText, getAllByText, push } = render(
      <WithSessions />
    );

    expect(getAllByText('Mac OS X • Chrome').length).toEqual(2);
    expect(
      queryByText('Unknown location - 127.0.0.1 - less than 5 seconds ago')
    ).toBeInTheDocument();
    expect(
      queryByText('Unknown location - 127.0.0.1 - about 1 hour ago')
    ).toBeInTheDocument();

    userEvent.click(
      getByText('Unknown location - 127.0.0.1 - less than 5 seconds ago')
    );

    sandbox.assert.calledWithExactly(
      push,
      '/sessions/[id]',
      `/sessions/${INSIGHT_SESSION.id}`,
      { shallow: undefined }
    );
  });
});
