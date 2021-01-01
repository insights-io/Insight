import { sandbox } from '@rebrowse/testing';
import { screen } from '@testing-library/react';
import { getPage } from 'next-page-tester';
import { ACCOUNT_SETTINGS_PAGE } from 'shared/constants/routes';
import { REBROWSE_ADMIN_DTO } from '__tests__/data';
import { mockAccountSettingsDetailsPage } from '__tests__/mocks';
import { renderPage } from '__tests__/utils';

describe('/settings/account', () => {
  /* Data */
  const route = ACCOUNT_SETTINGS_PAGE;
  const fullName = REBROWSE_ADMIN_DTO.fullName as string;

  test('As a user I get redirected to /settings/account/detaisl page', async () => {
    /* Mocks */
    document.cookie = 'SessionId=123';
    mockAccountSettingsDetailsPage(sandbox);

    /* Server */
    const { page } = await getPage({ route });

    /* Client */
    renderPage(page);

    expect(screen.getByDisplayValue(fullName)).toBeInTheDocument();
  });
});
