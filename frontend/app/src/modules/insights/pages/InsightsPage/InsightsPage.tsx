import React from 'react';
import AppLayout from 'modules/app/components/AppLayout';
import type { UserDTO } from '@insight/types';
import CountByDeviceClass from 'modules/insights/components/CountByDeviceClass';
import { Block } from 'baseui/block';
import { CardProps } from 'baseui/card';
import LocationDistribution from 'modules/insights/components/LocationDistribution';
import { CountByLocation } from 'modules/insights/components/charts/CountByLocationMapChart/utils';
import { useStyletron } from 'baseui';
import { useUser } from 'shared/hooks/useUser';

type Props = {
  user: UserDTO;
  countByLocation: CountByLocation;
  countByDeviceClass: Record<string, number>;
};

const countByCardOverrides = {
  Root: {
    overrides: {
      Contents: {
        style: { marginTop: 0, marginLeft: 0, marginBottom: 0, marginRight: 0 },
      },
      Root: { style: { flex: 1 } },
    },
  } as CardProps,
};

export const InsightsPage = ({
  countByLocation,
  countByDeviceClass,
  user: initialUser,
}: Props) => {
  const { user } = useUser(initialUser);
  const [_css, theme] = useStyletron();

  return (
    <AppLayout
      user={user}
      overrides={{
        MainContent: {
          style: {
            padding: theme.sizing.scale600,
            background: theme.colors.mono300,
          },
        },
      }}
    >
      <LocationDistribution countByLocation={countByLocation} />
      <Block display="flex" width="100%" marginTop={theme.sizing.scale600}>
        <CountByDeviceClass
          data={countByDeviceClass}
          overrides={countByCardOverrides}
        />
      </Block>
    </AppLayout>
  );
};
