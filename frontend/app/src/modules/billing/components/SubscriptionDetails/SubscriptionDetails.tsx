import React, { useState } from 'react';
import {
  subscriptionPlanText,
  subscriptionStatusIcon,
  subscriptionStatusText,
} from 'modules/billing/utils';
import { SIZE } from 'baseui/button';
import { useStyletron } from 'baseui';
import { Block } from 'baseui/block';
import { Accordion, Panel as BaseuiPanel } from 'baseui/accordion';
import { BillingApi } from 'api';
import type {
  APIError,
  APIErrorDataResponse,
  Invoice,
  Subscription,
  SubscriptionDTO,
} from '@rebrowse/types';
import { toaster } from 'baseui/toast';
import { Panel, Button, VerticalAligned, Flex } from '@rebrowse/elements';
import { format } from 'date-fns';

import { InvoiceList } from '../InvoiceList';

type Props = {
  subscription: Subscription;
  invoices: Invoice[];
  onSubscriptionCanceled: (subscription: SubscriptionDTO) => void;
};

export const SubscriptionDetails = ({
  subscription,
  invoices,
  onSubscriptionCanceled,
}: Props) => {
  const [isCanceling, setIsCanceling] = useState(false);
  const [_formError, setFormError] = useState<APIError>();

  const [_css, theme] = useStyletron();

  const cancelSubscription = async () => {
    if (isCanceling) {
      return;
    }

    setIsCanceling(true);

    BillingApi.subscriptions
      .cancel(subscription.id)
      .then((canceledSubscription) => {
        onSubscriptionCanceled(canceledSubscription);
        toaster.positive('Successfully canceled subscription', {});
      })
      .catch(async (error) => {
        const errorDTO: APIErrorDataResponse = await error.response.json();
        setFormError(errorDTO.error);
        toaster.negative('Something went wrong', {});
      })
      .finally(() => setIsCanceling(false));
  };

  return (
    <Panel>
      <Panel.Header>Subscription details</Panel.Header>
      <Panel.Item>
        <Flex>{`Plan: ${subscriptionPlanText(subscription.plan)}`}</Flex>
        <Flex marginTop={theme.sizing.scale300}>
          <VerticalAligned>
            {`Status: ${subscriptionStatusText(subscription.status)}`}
          </VerticalAligned>{' '}
          <VerticalAligned>
            {subscriptionStatusIcon[subscription.status](theme)}
          </VerticalAligned>
        </Flex>
        <Flex marginTop={theme.sizing.scale300}>
          <span>
            Created on:{' '}
            <span>{format(subscription.createdAt, 'MMM d, yyyy, HH:mm')}</span>
          </span>
        </Flex>

        {subscription.canceledAt && (
          <Flex marginTop={theme.sizing.scale300}>
            <span>
              Canceled on:{' '}
              <span>
                {format(subscription.canceledAt, 'MMM d, yyyy, HH:mm')}
              </span>
            </span>
          </Flex>
        )}

        {invoices.length > 0 && (
          <Block marginBottom={theme.sizing.scale600}>
            <Accordion>
              <BaseuiPanel
                title="Invoices"
                overrides={{
                  Content: {
                    style: {
                      paddingLeft: 0,
                      paddingRight: 0,
                      paddingTop: 0,
                      paddingBottom: 0,
                    },
                  },
                }}
              >
                <InvoiceList invoices={invoices} />
              </BaseuiPanel>
            </Accordion>
          </Block>
        )}

        {subscription.status === 'active' && (
          <Block marginTop={theme.sizing.scale600}>
            <Button
              kind="secondary"
              size={SIZE.compact}
              isLoading={isCanceling}
              disabled={isCanceling}
              onClick={cancelSubscription}
            >
              {subscriptionStatusIcon.canceled(theme)} Terminate
            </Button>
          </Block>
        )}
      </Panel.Item>
    </Panel>
  );
};
