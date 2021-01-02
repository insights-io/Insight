import { sandbox } from '@rebrowse/testing';
import { screen } from '@testing-library/react';
import { getPage } from 'next-page-tester';
import { ORGANIZATION_SETTINGS_BILLING_USAGE_AND_PAYMENTS_PAGE } from 'shared/constants/routes';
import { match } from 'sinon';
import { mockOrganizationSettingsUsageAndPaymentsPage } from '__tests__/mocks';
import { renderPage } from '__tests__/utils';

describe('/settings/organization/usage-and-payments', () => {
  /* Data */
  const route = ORGANIZATION_SETTINGS_BILLING_USAGE_AND_PAYMENTS_PAGE;

  test('As a user I can see details about usage & payments', async () => {
    /* Mocks */
    document.cookie = 'SessionId=123';
    const { listInvoicesStub } = mockOrganizationSettingsUsageAndPaymentsPage(
      sandbox
    );

    /* Server */
    const { page } = await getPage({ route });

    sandbox.assert.calledWithExactly(listInvoicesStub, {
      baseURL: 'http://localhost:8083',
      headers: {
        cookie: 'SessionId=123',
        'uber-trace-id': (match.string as unknown) as string,
      },
    });

    /* Client */
    renderPage(page);

    // Breadcrumb, Sidebar & Title
    expect(screen.getAllByText('Usage & Payments').length).toEqual(3);
    expect(screen.getByText('Amount: 1500 usd')).toBeInTheDocument();
  });
});
