import React, { useState } from 'react';
import {
  subscriptionPlanText,
  subscriptionStatusIcon,
  subscriptionStatusText,
} from 'billing/utils';
import { SIZE } from 'baseui/button';
import { useStyletron } from 'baseui';
import { Block } from 'baseui/block';
import { Accordion, Panel as BaseuiPanel } from 'baseui/accordion';
import type {
  APIError,
  APIErrorDataResponse,
  InvoiceDTO,
  SubscriptionDTO,
} from '@rebrowse/types';
import { toaster } from 'baseui/toast';
import { Panel, Button, VerticalAligned, Flex } from '@rebrowse/elements';
import { format } from 'date-fns';
import { useInvoices } from 'billing/hooks/useInvoices';
import { useSubscription } from 'billing/hooks/useSubscription';

import { InvoiceList } from '../InvoiceList';

type Props = {
  subscription: SubscriptionDTO;
  invoices: InvoiceDTO[];
};

export const SubscriptionDetails = ({
  subscription: initialSubscription,
  invoices: initialInvoices,
}: Props) => {
  const { subscription, cancelSubscription } = useSubscription(
    initialSubscription
  );
  const { invoices } = useInvoices(subscription.id, initialInvoices);
  const [isCanceling, setIsCanceling] = useState(false);
  const [_formError, setFormError] = useState<APIError>();

  const [_css, theme] = useStyletron();

  const onCancel = async () => {
    if (isCanceling) {
      return;
    }

    setIsCanceling(true);
    cancelSubscription()
      .then(() => toaster.positive('Successfully canceled subscription', {}))
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
              kind="tertiary"
              size={SIZE.compact}
              isLoading={isCanceling}
              disabled={isCanceling}
              onClick={onCancel}
            >
              {subscriptionStatusIcon.canceled(theme)} Terminate
            </Button>
          </Block>
        )}
      </Panel.Item>
    </Panel>
  );
};
