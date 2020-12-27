import React, { PropsWithChildren } from 'react';
import { useStyletron } from 'baseui';
import { ListItem, ListItemLabel, StyledRoot } from 'baseui/list';
import { StatefulTooltip } from 'baseui/tooltip';
import { ChevronRight } from 'baseui/icon';
import {
  subscriptionPlanText,
  subscriptionStatusIcon,
  subscriptionStatusText,
} from 'billing/utils';
import { ORGANIZATION_SETTINGS_BILLING_SUBSCRIPTION_PAGE } from 'shared/constants/routes';
import Link from 'next/link';
import type { StyleObject } from 'styletron-react';
import type { Subscription } from '@rebrowse/types';
import { Block } from 'baseui/block';
import { format } from 'date-fns';

type SubscriptionListElementProps = PropsWithChildren<{
  link: string;
  css: (arg: StyleObject) => string;
}>;

const SubscriptionListElement = ({
  children,
  link,
  css,
}: SubscriptionListElementProps) => {
  const linkStyles = css({
    textDecoration: 'none',
    color: 'inherit',
    display: 'flex',
    width: '100%',
  });

  return (
    <StyledRoot>
      <Link href={link}>
        <a href={link} className={linkStyles}>
          {children}
        </a>
      </Link>
    </StyledRoot>
  );
};

type Props = {
  subscriptions: Subscription[];
};

export const SubscriptionList = ({ subscriptions }: Props) => {
  const [css, theme] = useStyletron();

  return (
    <Block as="ul" className="subscriptions" padding={0} margin={0}>
      {subscriptions.map((subscription) => {
        const status = subscriptionStatusText(subscription.status);
        const label = subscriptionPlanText(subscription.plan);
        const description = `Created: ${format(
          subscription.createdAt,
          'MMM d, yyyy, HH:mm'
        )}`;
        const artwork = subscriptionStatusIcon[subscription.status](theme);
        const link = `${ORGANIZATION_SETTINGS_BILLING_SUBSCRIPTION_PAGE}/${subscription.id}`;

        return (
          <ListItem
            key={subscription.id}
            overrides={{
              Root: {
                component: SubscriptionListElement as React.ComponentType,
                props: { link, css },
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
            <ListItemLabel description={description}>
              {label} - {subscription.id}
            </ListItemLabel>
          </ListItem>
        );
      })}
    </Block>
  );
};
