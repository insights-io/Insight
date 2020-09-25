import React from 'react';
import type { Subscription } from '@insight/types';
import { useStyletron } from 'baseui';
import { ListItem, ListItemLabel } from 'baseui/list';
import { StatefulTooltip } from 'baseui/tooltip';
import { Check, ChevronRight } from 'baseui/icon';

type Props = {
  subscriptions: Subscription[];
};

export const SubscriptionList = ({ subscriptions }: Props) => {
  const [css, theme] = useStyletron();

  return (
    <ul className={css({ paddingLeft: 0, paddingRight: 0 })}>
      {subscriptions.map((subscription) => {
        return (
          <ListItem
            key={subscription.id}
            artwork={() => (
              <StatefulTooltip
                content={subscription.status}
                showArrow
                placement="top"
              >
                <Check color={theme.colors.positive400} size={24} />
              </StatefulTooltip>
            )}
            endEnhancer={() => <ChevronRight />}
          >
            <ListItemLabel
              description={`Created: ${subscription.createdAt.toLocaleDateString()}`}
            >
              Insight {subscription.plan}
            </ListItemLabel>
          </ListItem>
        );
      })}
    </ul>
  );
};
