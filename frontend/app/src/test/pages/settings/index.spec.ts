import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { getPage } from 'next-page-tester';
import {
  ACCOUNT_SETTINGS_DETAILS_PAGE,
  SETTINGS_PAGE,
} from 'shared/constants/routes';
import { mockAuth } from 'test/mocks';

describe('/settings', () => {
  /* Data */
  const route = SETTINGS_PAGE;

  test('As a user I see & search for basic information', async () => {
    /* Mocks */
    document.cookie = 'SessionId=123';
    mockAuth();

    /* Render */
    const { page } = await getPage({ route });
    render(page);

    /* Assertions */
    expect((await screen.findByText('AA')).closest('a')).toHaveAttribute(
      'href',
      ACCOUNT_SETTINGS_DETAILS_PAGE
    );

    userEvent.click(screen.getByText('Search'));
    expect(
      screen.getByText(
        'Password policy is a set of rules that define complexity requirements for your organization members'
      )
    ).toBeInTheDocument();

    userEvent.click(screen.getByText('Rebrowse'));

    await screen.findByDisplayValue('000000');
  });
});
