import React, { useMemo } from 'react';
import { AppLayout } from 'modules/app/components/AppLayout';
import type { OrganizationDTO, UserDTO } from '@rebrowse/types';
import type { CountByLocation } from 'modules/insights/components/charts/CountByLocationMapChart/utils';
import { useStyletron } from 'baseui';
import { useUser } from 'shared/hooks/useUser';
import { useOrganization } from 'shared/hooks/useOrganization';
import Head from 'next/head';
import { StatCard } from 'modules/insights/components/StatCard';
import { Block } from 'baseui/block';
import { LocationDistribution } from 'modules/insights/components/LocationDistribution';
import { Flex } from '@rebrowse/elements';
import { CountByDeviceClass } from 'modules/insights/components/CountByDeviceClass';
import type { CardProps } from 'baseui/card';

export type CountByDateDataPoint = { count: number; createdAt: string };

export type InsightsPageProps = {
  user: UserDTO;
  organization: OrganizationDTO;
  countByLocation: CountByLocation;
  countByDeviceClass: Record<string, number>;
  countSessionsByDate: CountByDateDataPoint[];
  countPageVisitsByDate: CountByDateDataPoint[];
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
  user: initialUser,
  organization: initialOrganization,
  countSessionsByDate: initialCountSessionsByDate,
  countPageVisitsByDate: initialCountPageVisitsByDate,
  countByDeviceClass,
  countByLocation,
}: InsightsPageProps) => {
  const { user } = useUser(initialUser);
  const { organization } = useOrganization(initialOrganization);
  const [_css, theme] = useStyletron();

  const countSessionsByDate = useMemo(
    () =>
      initialCountSessionsByDate.map((v) => ({
        value: v.count,
        date: new Date(v.createdAt),
      })),
    [initialCountSessionsByDate]
  );

  const countPageVisitsByDate = useMemo(
    () =>
      initialCountPageVisitsByDate.map((v) => ({
        value: v.count,
        date: new Date(v.createdAt),
      })),
    [initialCountPageVisitsByDate]
  );

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
      <Head>
        <title>Insights</title>
      </Head>

      <Block
        display="grid"
        gridGap={theme.sizing.scale800}
        gridTemplateColumns="repeat(auto-fit, minmax(400px, 1fr))"
      >
        <StatCard
          data={countPageVisitsByDate}
          title="Page Visits"
          timeRange="Last 30 days"
        />
        <StatCard
          data={countSessionsByDate}
          title="Sessions"
          timeRange="Last 30 days"
        />
      </Block>

      <Block marginTop={theme.sizing.scale800}>
        <LocationDistribution countByLocation={countByLocation} />
        <Flex width="100%" marginTop={theme.sizing.scale600}>
          <CountByDeviceClass
            data={countByDeviceClass}
            overrides={countByCardOverrides}
          />
        </Flex>
      </Block>
    </AppLayout>
  );
};
