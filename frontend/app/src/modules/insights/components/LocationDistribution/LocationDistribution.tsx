import React, { useState, useMemo, ReactText } from 'react';
import { Card, StyledBody } from 'baseui/card';
import { Block } from 'baseui/block';
import { HeadingXSmall } from 'baseui/typography';
import { useStyletron } from 'baseui';
import dynamic from 'next/dynamic';
import { Tabs, Tab } from 'baseui/tabs-motion';
import useWindowSize from 'shared/hooks/useWindowSize';

import { CountByLocation } from '../charts/CountByLocationMapChart/utils';
import CountByLocationDataTable from '../tables/CountByLocationDataTable';

const CountByCountryChart = dynamic(
  () => import('modules/insights/components/charts/CountByCountry'),
  { ssr: false }
);

const CountByContinentChart = dynamic(
  () => import('modules/insights/components/charts/CountByContinent'),
  { ssr: false }
);

const CountByLocationMapChart = dynamic(
  () => import('modules/insights/components/charts/CountByLocationMapChart'),
  { ssr: false }
);

type Props = {
  countByLocation: CountByLocation;
};

const LocationDistribution = ({ countByLocation }: Props) => {
  const { width = 0 } = useWindowSize();
  const [_css, theme] = useStyletron();
  const [activeKey, setActiveKey] = useState<ReactText>('0');

  const countByCountry = useMemo(
    () =>
      countByLocation.reduce((acc, entry) => {
        return { ...acc, [entry['location.countryName']]: entry.count };
      }, {} as Record<string, number>),
    [countByLocation]
  );

  const countByContinent = useMemo(
    () =>
      countByLocation.reduce((acc, entry) => {
        const key = entry['location.continentName'];
        const count = (acc[key] || 0) + entry.count;
        return { ...acc, [key]: count };
      }, {} as Record<string, number>),
    [countByLocation]
  );

  const isColumnOriented = width < 910;
  const tabHeight = '400px';

  return (
    <Card
      overrides={{
        Root: {
          style: {
            borderBottomRightRadius: theme.sizing.scale100,
            borderTopRightRadius: theme.sizing.scale100,
            borderTopLeftRadius: theme.sizing.scale100,
            borderBottomLeftRadius: theme.sizing.scale100,
            position: 'relative',
          },
        },
        Contents: {
          style: {
            marginTop: 0,
            marginRight: 0,
            marginBottom: 0,
            marginLeft: 0,
          },
        },
      }}
    >
      <StyledBody>
        <HeadingXSmall
          margin={0}
          position="absolute"
          top={theme.sizing.scale500}
          left={theme.sizing.scale500}
        >
          Location distribution
        </HeadingXSmall>

        <Tabs
          activeKey={activeKey}
          onChange={(params) => {
            setActiveKey(params.activeKey);
          }}
          activateOnFocus
          renderAll={false}
          overrides={{
            TabList: {
              style: { justifyContent: 'flex-end' },
            },
          }}
        >
          <Tab title="Pie chart">
            <Block
              padding={theme.sizing.scale400}
              display="flex"
              flexDirection={isColumnOriented ? 'column' : 'row'}
            >
              <CountByContinentChart
                data={countByContinent}
                height={tabHeight}
              />
              <CountByCountryChart data={countByCountry} height={tabHeight} />
            </Block>
          </Tab>
          <Tab title="Table">
            <CountByLocationDataTable
              data={countByLocation}
              height={tabHeight}
            />
          </Tab>
          <Tab title="Map">
            <CountByLocationMapChart
              data={countByLocation}
              height={tabHeight}
            />
          </Tab>
        </Tabs>
      </StyledBody>
    </Card>
  );
};
export default LocationDistribution;
