import React, { useState } from 'react';
import { Card, StyledBody, StyledAction } from 'baseui/card';
import {
  capitalize,
  invoiceStatusIcon,
  subscriptionPlanText,
  subscriptionStatusIcon,
  subscriptionStatusText,
} from 'modules/billing/utils';
import { Button, SHAPE, SIZE } from 'baseui/button';
import { useStyletron } from 'baseui';
import Divider from 'shared/components/Divider';
import { Block } from 'baseui/block';
import { Accordion, Panel } from 'baseui/accordion';
import { ListItem, ListItemLabel } from 'baseui/list';
import { StatefulTooltip } from 'baseui/tooltip';
import { FaFileDownload, FaLink } from 'react-icons/fa';
import { ExternalLink } from 'shared/components/ExternalLink';
import { BillingApi } from 'api';
import type {
  APIError,
  APIErrorDataResponse,
  Invoice,
  Subscription,
  SubscriptionDTO,
} from '@insight/types';
import { toaster } from 'baseui/toast';

type Props = {
  subscription: Subscription;
  invoices: Invoice[];
  onSubscriptionUpdated: (subscription: SubscriptionDTO) => void;
};

export const SubscriptionDetails = ({
  subscription,
  invoices,
  onSubscriptionUpdated,
}: Props) => {
  const [isCanceling, setIsCanceling] = useState(false);
  const [_formError, setFormError] = useState<APIError>();

  const [css, theme] = useStyletron();
  const title = subscriptionPlanText(subscription.plan);

  const cancelSubscription = async () => {
    if (isCanceling) {
      return;
    }

    setIsCanceling(true);

    BillingApi.subscriptions
      .cancel(subscription.id)
      .then((canceledSubscription) => {
        onSubscriptionUpdated(canceledSubscription);
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
    <Card title={title}>
      <Divider />
      <StyledBody>
        <Block>Status: {subscriptionStatusText(subscription.status)}</Block>
        <Block>Created on: {subscription.createdAt.toLocaleDateString()}</Block>
        {subscription.canceledAt && (
          <Block>
            Canceled on: {subscription.canceledAt.toLocaleDateString()}
          </Block>
        )}
      </StyledBody>
      <Divider />

      <Block marginBottom={theme.sizing.scale600}>
        <Accordion>
          <Panel
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
            <ul className={css({ padding: 0 })}>
              {invoices.map((invoice) => {
                const artwork = invoiceStatusIcon[invoice.status](theme);

                return (
                  <ListItem
                    key={invoice.id}
                    artwork={() => (
                      <StatefulTooltip
                        content={capitalize(invoice.status)}
                        placement="top"
                        showArrow
                      >
                        {artwork}
                      </StatefulTooltip>
                    )}
                    endEnhancer={() => (
                      <>
                        <StatefulTooltip
                          content="Download invoice"
                          placement="top"
                          showArrow
                        >
                          <ExternalLink
                            link={`${invoice.link}/pdf`}
                            data-testid="invoice-pdf"
                          >
                            <Button size={SIZE.compact} shape={SHAPE.pill}>
                              <FaFileDownload />
                            </Button>
                          </ExternalLink>
                        </StatefulTooltip>
                        <StatefulTooltip
                          content="Open invoice"
                          placement="top"
                          showArrow
                        >
                          <ExternalLink
                            link={invoice.link}
                            className={css({
                              marginLeft: theme.sizing.scale400,
                            })}
                          >
                            <Button
                              size={SIZE.compact}
                              shape={SHAPE.pill}
                              data-testid="invoice-link"
                            >
                              <FaLink />
                            </Button>
                          </ExternalLink>
                        </StatefulTooltip>
                      </>
                    )}
                  >
                    <ListItemLabel>
                      Amount: {invoice.amountDue} {invoice.currency}
                    </ListItemLabel>
                  </ListItem>
                );
              })}
            </ul>
          </Panel>
        </Accordion>
      </Block>

      {subscription.status === 'active' && (
        <StyledAction>
          <Button
            kind="secondary"
            size={SIZE.compact}
            shape={SHAPE.pill}
            isLoading={isCanceling}
            onClick={cancelSubscription}
          >
            {subscriptionStatusIcon.canceled(theme)} Cancel
          </Button>
        </StyledAction>
      )}
    </Card>
  );
};
