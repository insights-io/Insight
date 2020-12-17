import React, { useMemo } from 'react';
import { AppLayout } from 'modules/app/components/AppLayout';
import type { OrganizationDTO, UserDTO } from '@rebrowse/types';
import { useStyletron } from 'baseui';
import { useUser } from 'shared/hooks/useUser';
import { useOrganization } from 'shared/hooks/useOrganization';
import Head from 'next/head';
import { StatCard } from 'modules/insights/components/StatCard';
import { Block } from 'baseui/block';
import { ResponsivePieChart } from 'modules/insights/components/PieChart';
import { scaleOrdinal } from '@visx/scale';
import { InsightCard } from 'modules/insights/components/InsightCard';

type CountByFieldDataPoint<Field extends string> = {
  [key in Field]: string;
} & {
  count: number;
};

export type CountByDateDataPoint = CountByFieldDataPoint<'createdAt'>;
export type CountByDeviceClassDataPoint = CountByFieldDataPoint<'userAgent.deviceClass'>;
export type CountByLocationDataPoint = CountByFieldDataPoint<
  'location.countryName' | 'location.continentName'
>;
type CountByContinentNameDataPoint = CountByFieldDataPoint<'location.continentName'>;
type CountyByCountryNameDataPoint = CountByFieldDataPoint<'location.countryName'>;

export type InsightsPageProps = {
  user: UserDTO;
  organization: OrganizationDTO;
  countSessionsByLocation: CountByLocationDataPoint[];
  countSessionsByDeviceClass: CountByDeviceClassDataPoint[];
  countSessionsByDate: CountByDateDataPoint[];
  countPageVisitsByDate: CountByDateDataPoint[];
};

const getDeviceClassColor = scaleOrdinal({
  domain: ['Phone', 'Desktop'],
  range: [
    'rgba(255,255,255,0.7)',
    'rgba(255,255,255,0.6)',
    'rgba(255,255,255,0.5)',
  ],
});

const getCountyColor = scaleOrdinal({
  domain: ['Unknown', 'Slovenia', 'United States'],
  range: [
    'rgba(255,255,255,0.7)',
    'rgba(255,255,255,0.6)',
    'rgba(255,255,255,0.5)',
  ],
});

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

  const countSessionsByContinentName = useMemo(() => {
    const map = countSessionsByLocation.reduce((acc, item) => {
      const value = acc[item['location.continentName']];
      return {
        ...acc,
        [item['location.continentName']]: (value || 0) + item.count,
      };
    }, {} as Record<string, number>);

    return Object.keys(map).reduce(
      (acc, key) => [
        ...acc,
        { 'location.continentName': key, count: map[key] },
      ],
      [] as CountByContinentNameDataPoint[]
    );
  }, [countSessionsByLocation]);

  const countSessionsByCountryName = useMemo(() => {
    const map = countSessionsByLocation.reduce((acc, item) => {
      const value = acc[item['location.countryName']];
      return {
        ...acc,
        [item['location.countryName']]: (value || 0) + item.count,
      };
    }, {} as Record<string, number>);

    return Object.keys(map).reduce(
      (acc, key) => [...acc, { 'location.countryName': key, count: map[key] }],
      [] as CountyByCountryNameDataPoint[]
    );
  }, [countSessionsByLocation]);

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
        <InsightCard height="400px">
          <InsightCard.Title>Device distribution</InsightCard.Title>
          <InsightCard.Subtitle>Last 30 days</InsightCard.Subtitle>
          <InsightCard.Content>
            <ResponsivePieChart
              data={countSessionsByDeviceClass}
              getLabel={(d) => d['userAgent.deviceClass']}
              getColor={(d) => getDeviceClassColor(d['userAgent.deviceClass'])}
              getPieValue={(d) => d.count}
            />
          </InsightCard.Content>
        </InsightCard>

        <InsightCard height="400px">
          <InsightCard.Title>Country distribution</InsightCard.Title>
          <InsightCard.Subtitle>Last 30 days</InsightCard.Subtitle>
          <InsightCard.Content>
            <ResponsivePieChart
              data={countSessionsByCountryName}
              getLabel={(d) => d['location.countryName']}
              getColor={(d) => getCountyColor(d['location.countryName'])}
              getPieValue={(d) => d.count}
            />
          </InsightCard.Content>
        </InsightCard>

        <InsightCard height="400px">
          <InsightCard.Title>Continent distribution</InsightCard.Title>
          <InsightCard.Subtitle>Last 30 days</InsightCard.Subtitle>
          <InsightCard.Content>
            <ResponsivePieChart
              data={countSessionsByContinentName}
              getLabel={(d) => d['location.continentName']}
              getColor={(d) => getCountyColor(d['location.continentName'])}
              getPieValue={(d) => d.count}
            />
          </InsightCard.Content>
        </InsightCard>
      </Block>
    </AppLayout>
  );
};
