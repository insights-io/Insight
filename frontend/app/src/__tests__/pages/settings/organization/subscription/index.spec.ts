import { sandbox } from '@rebrowse/testing';
import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { getPage } from 'next-page-tester';
import { ORGANIZATION_SETTINGS_BILLING_SUBSCRIPTION_PAGE } from 'shared/constants/routes';
import { match } from 'sinon';
import { FREE_PLAN_DTO } from '__tests__/data/billing';
import { mockOrganizationSettingsSubscriptionPage } from '__tests__/mocks';
import { renderPage } from '__tests__/utils';

describe('/settings/organization/subscription', () => {
  /* Data */
  const route = ORGANIZATION_SETTINGS_BILLING_SUBSCRIPTION_PAGE;

  describe('Free plan', () => {
    test('As a user I can see details about subscription plan & upgrade it', async () => {
      /* Mocks */
      document.cookie = 'SessionId=123';
      const {
        listSubscriptionsStub,
        retrieveActivePlanStub,
      } = mockOrganizationSettingsSubscriptionPage(sandbox, {
        plan: FREE_PLAN_DTO,
      });

      /* Server */
      const { page } = await getPage({ route });

      sandbox.assert.calledWithExactly(listSubscriptionsStub, {
        headers: {
          cookie: 'SessionId=123',
          'uber-trace-id': (match.string as unknown) as string,
        },
        search: { sortBy: ['-createdAt'] },
      });
      sandbox.assert.calledWithExactly(retrieveActivePlanStub, {
        headers: {
          cookie: 'SessionId=123',
          'uber-trace-id': (match.string as unknown) as string,
        },
      });

      /* Client */
      renderPage(page);

      expect(screen.getByText('Rebrowse Free')).toBeInTheDocument();
      expect(screen.getByText('0 of 1,000 sessions')).toBeInTheDocument();

      userEvent.click(screen.getByText('Upgrade'));

      // TODO: figure out how to mock StripeJS
      await screen.findByText('Pay');
    });
  });

  describe('Genesis plan', () => {
    test('As a user I can see details about subscription plan', async () => {
      /* Mocks */
      document.cookie = 'SessionId=123';
      const {
        listSubscriptionsStub,
        retrieveActivePlanStub,
      } = mockOrganizationSettingsSubscriptionPage(sandbox);

      /* Server */
      const { page } = await getPage({ route });

      sandbox.assert.calledWithExactly(listSubscriptionsStub, {
        headers: {
          cookie: 'SessionId=123',
          'uber-trace-id': (match.string as unknown) as string,
        },
        search: { sortBy: ['-createdAt'] },
      });
      sandbox.assert.calledWithExactly(retrieveActivePlanStub, {
        headers: {
          cookie: 'SessionId=123',
          'uber-trace-id': (match.string as unknown) as string,
        },
      });

      /* Client */
      renderPage(page);

      expect(screen.getByText('Rebrowse Enterprise')).toBeInTheDocument();
      expect(screen.getByText('0 of ∞ sessions')).toBeInTheDocument();
    });
  });
});
