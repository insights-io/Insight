import React from 'react';
import { render } from 'test/utils';
import userEvent from '@testing-library/user-event';
import { sandbox } from '@rebrowse/testing';
import { REBROWSE_SESSIONS } from 'test/data';
import { AutoSizerProps } from 'react-virtualized-auto-sizer';
import { screen, waitFor } from '@testing-library/react';

import { NoSessions, WithSessions } from './SessionsPage.stories';

jest.mock('react-virtualized-auto-sizer', () => {
  return {
    __esModule: true,
    default: ({ children }: AutoSizerProps) => {
      return children({ width: 1000, height: 1000 });
    },
  };
});

// TODO: improve test
const MAC_CHROME = 'Mac OS X • Chrome';

const LJUBLJANA_LOCATION = /^Ljubljana, Slovenia - 82.192.62.51 - less than [1-9][0-9]* seconds ago$/;

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
    const { container } = render(<WithSessions />);

    expect(screen.getAllByText(MAC_CHROME).length).toEqual(16);
    expect(screen.getByText(LJUBLJANA_LOCATION)).toBeInTheDocument();

    userEvent.click(screen.getByText('0 Filters'));
    userEvent.click(screen.getByText('Filter event by...'));
    userEvent.click(screen.getByText('City'));
    sandbox.assert.calledWithExactly(getDistinctStub, 'location.city');

    const autocompleteInput = screen.getByText('Type something').parentElement
      ?.firstChild?.firstChild as HTMLInputElement;

    const cityText = 'Boydton';
    await userEvent.type(autocompleteInput, cityText);

    await waitFor(() => {
      sandbox.assert.calledWithExactly(getSessionsStub, {
        search: {
          limit: 20,
          'location.city': `eq:${cityText}`,
          sortBy: ['-createdAt'],
        },
      });
      sandbox.assert.calledWithExactly(getSessionCountStub, {
        search: { 'location.city': `eq:${cityText}` },
      });
    });

    expect(screen.getAllByText(MAC_CHROME).length).toEqual(16);

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

    expect(screen.getAllByText(MAC_CHROME).length).toEqual(16);

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

    expect(screen.getAllByText(MAC_CHROME).length).toEqual(16);
  });

  it('Should be able to navigate to a session details page', async () => {
    WithSessions.story.setupMocks(sandbox);
    const { findByText, push } = render(<WithSessions />);

    userEvent.click(await findByText(LJUBLJANA_LOCATION));

    sandbox.assert.calledWithExactly(
      push,
      `/sessions/${REBROWSE_SESSIONS[0].id}`,
      `/sessions/${REBROWSE_SESSIONS[0].id}`,
      {
        shallow: undefined,
        locale: undefined,
      }
    );
  });
});
