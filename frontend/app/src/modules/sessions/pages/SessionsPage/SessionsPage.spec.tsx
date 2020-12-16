import React from 'react';
import { render } from 'test/utils';
import userEvent from '@testing-library/user-event';
import { sandbox } from '@rebrowse/testing';
import { REBROWSE_SESSION } from 'test/data';
import { AutoSizerProps } from 'react-virtualized-auto-sizer';
import { waitFor } from '@testing-library/react';

import { NoSessions, WithSessions } from './SessionsPage.stories';

jest.mock('react-virtualized-auto-sizer', () => {
  return {
    __esModule: true,
    default: ({ children }: AutoSizerProps) => {
      return children({ width: 1000, height: 1000 });
    },
  };
});

const MAC_CHROME = 'Mac OS X • Chrome';
const ANDROID_CHROME = 'Android • Chrome';

const LJUBLJANA_LOCATION = /^Ljubljana, Slovenia - 82.192.62.51 - less than [1-9][0-9]* seconds ago$/;
const BOYDTON_LOCATION =
  'Boydton, Virginia, United States - 13.77.88.76 - about 1 hour ago';

const UNKNOWN_LOCATION = 'Unknown location - 13.77.88.76 - 1 day ago';

describe('<SessionsPage />', () => {
  it('Should render recording snippet on no sessions', async () => {
    const { findByText } = render(<NoSessions />);

    await findByText("._i_org = '000000';", { exact: false });
    await findByText(".src = 'https://static.rebrowse.dev/s/rebrowse.js';", {
      exact: false,
    });
  });

  it('Should be able to filter sessions', async () => {
    const {
      getDistinctStub,
      getSessionCountStub,
      getSessionsStub,
    } = WithSessions.story.setupMocks(sandbox);
    const { queryByText, getByText, queryAllByText, container } = render(
      <WithSessions />
    );

    expect(queryAllByText(MAC_CHROME).length).toEqual(2);
    expect(queryAllByText(ANDROID_CHROME).length).toEqual(2);

    expect(queryByText(LJUBLJANA_LOCATION)).toBeInTheDocument();
    expect(queryByText(BOYDTON_LOCATION)).toBeInTheDocument();
    expect(queryByText(UNKNOWN_LOCATION)).toBeInTheDocument();

    userEvent.click(getByText('0 Filters'));
    userEvent.click(getByText('Filter event by...'));
    userEvent.click(getByText('City'));
    sandbox.assert.calledWithExactly(getDistinctStub, 'location.city');

    const autocompleteInput = getByText('Type something').parentElement
      ?.firstChild?.firstChild as HTMLInputElement;

    await userEvent.type(autocompleteInput, 'Maribor');

    await waitFor(() => {
      sandbox.assert.calledWithExactly(getSessionsStub, {
        search: {
          limit: 20,
          'location.city': 'eq:Maribor',
          sortBy: ['-createdAt'],
        },
      });
      sandbox.assert.calledWithExactly(getSessionCountStub, {
        search: { 'location.city': 'eq:Maribor' },
      });
    });

    expect(queryAllByText(MAC_CHROME).length).toEqual(0);
    expect(queryAllByText(ANDROID_CHROME).length).toEqual(0);

    const clearTextIcon = container.querySelector(
      'svg[aria-label="Clear value"]'
    ) as SVGElement;

    userEvent.click(clearTextIcon);
    await userEvent.type(autocompleteInput, 'Ljubljana');

    await waitFor(() => {
      sandbox.assert.calledWithExactly(getSessionsStub, {
        search: {
          limit: 20,
          'location.city': 'eq:Ljubljana',
          sortBy: ['-createdAt'],
        },
      });
      sandbox.assert.calledWithExactly(getSessionCountStub, {
        search: { 'location.city': 'eq:Ljubljana' },
      });
    });

    expect(queryAllByText(MAC_CHROME).length).toEqual(1);
    expect(queryAllByText(ANDROID_CHROME).length).toEqual(0);

    const removeFilterIcon = container.querySelector(
      'svg[title="Delete"]'
    ) as SVGElement;

    userEvent.click(removeFilterIcon);

    await waitFor(() => {
      sandbox.assert.calledWithExactly(getSessionsStub, {
        search: {
          limit: 20,
          sortBy: ['-createdAt'],
        },
      });
      sandbox.assert.calledWithExactly(getSessionCountStub, {
        search: {},
      });
    });

    expect(queryAllByText(MAC_CHROME).length).toEqual(2);
    expect(queryAllByText(ANDROID_CHROME).length).toEqual(2);
  });

  it('Should be able to navigate to a session details page', async () => {
    WithSessions.story.setupMocks(sandbox);
    const { findByText, push } = render(<WithSessions />);

    userEvent.click(await findByText(LJUBLJANA_LOCATION));

    sandbox.assert.calledWithExactly(
      push,
      `/sessions/${REBROWSE_SESSION.id}`,
      `/sessions/${REBROWSE_SESSION.id}`,
      {
        shallow: undefined,
        locale: undefined,
      }
    );
  });
});
