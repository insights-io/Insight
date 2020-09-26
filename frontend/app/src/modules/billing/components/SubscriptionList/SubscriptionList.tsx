import React from 'react';
import { useStyletron } from 'baseui';
import { ListItem, ListItemLabel } from 'baseui/list';
import { StatefulTooltip } from 'baseui/tooltip';
import { ChevronRight } from 'baseui/icon';
import {
  subscriptionPlanText,
  subscriptionStatusIcon,
  subscriptionStatusText,
} from 'modules/billing/utils';
import type { Subscription } from '@insight/types';

type Props = {
  subscriptions: Subscription[];
  onClick: (subscriptionId: string) => void;
};

export const SubscriptionList = ({ subscriptions, onClick }: Props) => {
  const [css, theme] = useStyletron();

  return (
    <ul className={css({ paddingLeft: 0, paddingRight: 0 })}>
      {subscriptions.map((subscription) => {
        const status = subscriptionStatusText(subscription.status);
        const label = subscriptionPlanText(subscription.plan);
        const description = `Created: ${subscription.createdAt.toLocaleDateString()}`;
        const artwork = subscriptionStatusIcon[subscription.status](theme);

        return (
          <ListItem
            key={subscription.id}
            overrides={{
              Root: {
                props: { onClick: () => onClick(subscription.id) },
                style: {
                  ':hover': {
                    background: theme.colors.primary200,
                    cursor: 'pointer',
                  },
                },
              },
            }}
            artwork={() => (
              <StatefulTooltip content={status} showArrow placement="top">
                {artwork}
              </StatefulTooltip>
            )}
            endEnhancer={() => <ChevronRight />}
          >
            <ListItemLabel description={description}>{label}</ListItemLabel>
          </ListItem>
        );
      })}
    </ul>
  );
};
