import React from 'react';
import { AppLayout } from 'modules/app/components/AppLayout';
import type { OrganizationDTO, UserDTO } from '@insight/types';
import CountByDeviceClass from 'modules/insights/components/CountByDeviceClass';
import type { CardProps } from 'baseui/card';
import LocationDistribution from 'modules/insights/components/LocationDistribution';
import type { CountByLocation } from 'modules/insights/components/charts/CountByLocationMapChart/utils';
import { useStyletron } from 'baseui';
import { useUser } from 'shared/hooks/useUser';
import { useOrganization } from 'shared/hooks/useOrganization';
import { Flex } from '@insight/elements';

type Props = {
  user: UserDTO;
  organization: OrganizationDTO;
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
  organization: initialOrganization,
}: Props) => {
  const { user } = useUser(initialUser);
  const { organization } = useOrganization(initialOrganization);
  const [_css, theme] = useStyletron();

  return (
    <AppLayout
      organization={organization}
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
      <Flex width="100%" marginTop={theme.sizing.scale600}>
        <CountByDeviceClass
          data={countByDeviceClass}
          overrides={countByCardOverrides}
        />
      </Flex>
    </AppLayout>
  );
};
