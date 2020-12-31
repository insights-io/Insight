import { sandbox } from '@rebrowse/testing';
import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { getPage } from 'next-page-tester';
import { ORGANIZATION_SETTINGS_BILLING_SUBSCRIPTION_PAGE } from 'shared/constants/routes';
import { match } from 'sinon';
import { ACTIVE_BUSINESS_SUBSCRIPTION_DTO } from '__tests__/data/billing';
import { mockOrganizationSettingsSubscriptionDetailsPage } from '__tests__/mocks';
import { renderPage } from '__tests__/utils';

describe('/settings/organization/subscription/[id]', () => {
  /* Data */
  const subscription = ACTIVE_BUSINESS_SUBSCRIPTION_DTO;
  const route = `${ORGANIZATION_SETTINGS_BILLING_SUBSCRIPTION_PAGE}/${subscription.id}`;

  test('As a user I should see details about active subscription & terminate it', async () => {
    /* Mocks */
    document.cookie = 'SessionId=123';
    const {
      listInvoicesStub,
      retrieveSubscriptionStub,
      cancelSubscriptionStub,
    } = mockOrganizationSettingsSubscriptionDetailsPage(sandbox, {
      subscription,
    });

    /* Server */
    const { page } = await getPage({ route });

    sandbox.assert.calledWithExactly(
      retrieveSubscriptionStub,
      subscription.id,
      {
        baseURL: 'http://localhost:8083',
        headers: {
          cookie: 'SessionId=123',
          'uber-trace-id': (match.string as unknown) as string,
        },
      }
    );
    sandbox.assert.calledWithExactly(listInvoicesStub, subscription.id, {
      baseURL: 'http://localhost:8083',
      headers: {
        cookie: 'SessionId=123',
        'uber-trace-id': (match.string as unknown) as string,
      },
    });

    /* Client */
    renderPage(page);

    expect(screen.getByText('Plan: Rebrowse Business')).toBeInTheDocument();
    expect(screen.getByText('Status: Active')).toBeInTheDocument();

    userEvent.click(screen.getByText('Invoices'));

    expect(screen.getByText('Amount: 1500 usd')).toBeInTheDocument();

    userEvent.click(screen.getByText('Terminate'));

    await screen.findByText('Successfully canceled subscription');
    expect(screen.getByText('Status: Canceled')).toBeInTheDocument();

    sandbox.assert.calledWithExactly(cancelSubscriptionStub, subscription.id);
  });
});
