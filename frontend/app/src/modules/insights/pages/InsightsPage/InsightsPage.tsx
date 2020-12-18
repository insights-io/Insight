import React, { useMemo } from 'react';
import { AppLayout } from 'modules/app/components/AppLayout';
import type { OrganizationDTO, UserDTO } from '@rebrowse/types';
import { useStyletron } from 'baseui';
import { useUser } from 'shared/hooks/useUser';
import { useOrganization } from 'shared/hooks/useOrganization';
import Head from 'next/head';
import { StatCard } from 'modules/insights/components/StatCard';
import { Block } from 'baseui/block';

import type {
  CountByDateDataPoint,
  CountByDeviceClassDataPoint,
  CountByLocationDataPoint,
} from './types';
import { PieChartBreakdown } from './PieChartBreakdown';

export type InsightsPageProps = {
  user: UserDTO;
  organization: OrganizationDTO;
  countSessionsByLocation: CountByLocationDataPoint[];
  countSessionsByDeviceClass: CountByDeviceClassDataPoint[];
  countSessionsByDate: CountByDateDataPoint[];
  countPageVisitsByDate: CountByDateDataPoint[];
};

export const InsightsPage = ({
  user: initialUser,
  organization: initialOrganization,
  countSessionsByDate: initialCountSessionsByDate,
  countPageVisitsByDate: initialCountPageVisitsByDate,
  countSessionsByDeviceClass,
  countSessionsByLocation,
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
        gridGap={theme.sizing.scale600}
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

      <Block
        display="grid"
        gridGap={theme.sizing.scale600}
        gridTemplateColumns="repeat(auto-fit, minmax(400px, 1fr))"
        marginTop={theme.sizing.scale600}
      >
        <PieChartBreakdown
          height="400px"
          title="Device Breakdown"
          subtitle="Last 30 days"
          data={countSessionsByDeviceClass}
          field="userAgent.deviceClass"
        />

        <PieChartBreakdown
          height="400px"
          title="Country Breakdown"
          subtitle="Last 30 days"
          data={countSessionsByLocation}
          field="location.countryName"
        />

        <PieChartBreakdown
          height="400px"
          title="Continent Breakdown"
          subtitle="Last 30 days"
          data={countSessionsByLocation}
          field="location.continentName"
        />
      </Block>
    </AppLayout>
  );
};
