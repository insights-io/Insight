import React, { useState } from 'react';
import { AppLayout } from 'shared/components/AppLayout';
import type { OrganizationDTO, UserDTO } from '@rebrowse/types';
import { useStyletron } from 'baseui';
import { useUser } from 'shared/hooks/useUser';
import { useOrganization } from 'shared/hooks/useOrganization';
import Head from 'next/head';
import { Block } from 'baseui/block';
import type { RelativeTimeRange, TimeRangeOption } from 'shared/utils/date';
import { Select, SIZE } from 'baseui/select';
import { expandBorderRadius } from '@rebrowse/elements';
import {
  countPageVisitsByDate,
  countSessionsByDate,
  countSessionsByDeviceClass,
  countSessionsByLocation,
} from 'insights/api';
import type {
  CountByDateDataPointDTO,
  CountByDeviceClassDataPoint,
  CountByLocationDataPoint,
} from 'insights/types';
import { PieChartBreakdown } from 'insights/containers/PieChartBreakdown';
import { LineChartBreakdown } from 'insights/containers/LineChartBreakdown';

const TIME_RANGE_OPTIONS: TimeRangeOption[] = [
  { label: 'Last hour', value: '1h' },
  { label: 'Last 24 hours', value: '24h' },
  { label: 'Last 7 days', value: '7d' },
  { label: 'Last 30 days', value: '30d' },
  { label: 'Last 90 days', value: '90d' },
];
const getInitialTimeRange = (relativeTimeRange: RelativeTimeRange) => {
  return [
    TIME_RANGE_OPTIONS.find((o) => o.value === relativeTimeRange) ||
      TIME_RANGE_OPTIONS[3],
  ];
};

export type InsightsPageProps = {
  user: UserDTO;
  organization: OrganizationDTO;
  sessionsByLocationCount: CountByLocationDataPoint[];
  sessionsByDeviceCount: CountByDeviceClassDataPoint[];
  sessionsByDateCount: CountByDateDataPointDTO[];
  pageVisitsByDateCount: CountByDateDataPointDTO[];
  relativeTimeRange: RelativeTimeRange;
};

export const InsightsPage = ({
  user: initialUser,
  organization: initialOrganization,
  sessionsByDateCount: initialCountSessionsByDate,
  pageVisitsByDateCount: initialCountPageVisitsByDate,
  sessionsByDeviceCount: initialSessionsByDeviceCount,
  sessionsByLocationCount: initialSessionsByLocationCount,
  relativeTimeRange: initialRelativeTimeRange,
}: InsightsPageProps) => {
  const { user } = useUser(initialUser);
  const { organization } = useOrganization(initialOrganization);
  const [_css, theme] = useStyletron();
  const [timeRange, setTimeRange] = useState(() =>
    getInitialTimeRange(initialRelativeTimeRange)
  );

  return (
    <AppLayout
      organization={organization}
      user={user}
      overrides={{
        MainContent: { style: { background: theme.colors.mono300 } },
      }}
    >
      <Head>
        <title>Insights</title>
      </Head>

      <Block
        display="grid"
        $style={{ border: '1px solid rgb(231, 225, 236)' }}
        backgroundColor={theme.colors.white}
        padding={theme.sizing.scale600}
        gridTemplateColumns="repeat(auto-fit, minmax(400px, 1fr))"
      >
        <div />
        <div />
        <Select
          options={TIME_RANGE_OPTIONS}
          value={timeRange}
          size={SIZE.compact}
          valueKey="value"
          onChange={(params) => {
            if (params.type === 'remove') {
              return;
            }
            setTimeRange(params.value as [TimeRangeOption]);
          }}
          clearable={false}
          overrides={{
            ControlContainer: { style: expandBorderRadius('8px') },
          }}
        />
      </Block>
      <Block as="section" padding={theme.sizing.scale600}>
        <Block
          display="grid"
          gridGap={theme.sizing.scale600}
          gridTemplateColumns="repeat(auto-fit, minmax(400px, 1fr))"
        >
          <LineChartBreakdown
            title="Page Visits"
            field="createdAt"
            initialData={initialCountPageVisitsByDate}
            relativeTimeRange={timeRange[0].value}
            dataLoader={countPageVisitsByDate}
          />
          <LineChartBreakdown
            title="Sessions"
            field="createdAt"
            initialData={initialCountSessionsByDate}
            relativeTimeRange={timeRange[0].value}
            dataLoader={countSessionsByDate}
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
            relativeTimeRange={timeRange[0].value}
            initialData={initialSessionsByDeviceCount}
            dataLoader={countSessionsByDeviceClass}
            field="userAgent.deviceClass"
          />

          <PieChartBreakdown
            height="400px"
            title="Country Breakdown"
            relativeTimeRange={timeRange[0].value}
            initialData={initialSessionsByLocationCount}
            dataLoader={countSessionsByLocation}
            field="location.countryName"
          />

          <PieChartBreakdown
            height="400px"
            title="Continent Breakdown"
            relativeTimeRange={timeRange[0].value}
            initialData={initialSessionsByLocationCount}
            dataLoader={countSessionsByLocation}
            field="location.continentName"
          />
        </Block>
      </Block>
    </AppLayout>
  );
};
