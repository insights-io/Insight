import React from 'react';
import {
  AuthenticatedServerSideProps,
  authenticated,
} from 'modules/auth/middleware/authMiddleware';
import { GetServerSideProps, GetServerSidePropsResult } from 'next';
import { startRequestSpan, prepareCrossServiceHeaders } from 'modules/tracing';
import {
  InsightsPage,
  CountByDateDataPoint,
} from 'modules/insights/pages/InsightsPage';
import { SessionApi, PagesApi } from 'api';
import { CountByLocation } from 'modules/insights/components/charts/CountByLocationMapChart/utils';
import { TimePrecision } from '@rebrowse/types';
import { addDays } from 'date-fns';

type Props = AuthenticatedServerSideProps & {
  countByLocation: CountByLocation;
  countByDeviceClass: Record<string, number>;
  countSessionsByDate: CountByDateDataPoint[];
  countPageVisitsByDate: CountByDateDataPoint[];
};

const Home = ({
  user,
  countByLocation,
  organization,
  countByDeviceClass,
  countSessionsByDate,
  countPageVisitsByDate,
}: Props) => {
  return (
    <InsightsPage
      user={user}
      countByLocation={countByLocation}
      countByDeviceClass={countByDeviceClass}
      organization={organization}
      countSessionsByDate={countSessionsByDate}
      countPageVisitsByDate={countPageVisitsByDate}
    />
  );
};

export const getServerSideProps: GetServerSideProps<Props> = async (
  context
) => {
  const requestSpan = startRequestSpan(context.req);
  try {
    const authResponse = await authenticated(context, requestSpan);
    if (!authResponse) {
      return ({ props: {} } as unknown) as GetServerSidePropsResult<Props>;
    }

    const countSessionsByDatePromise = SessionApi.count<CountByDateDataPoint[]>(
      {
        baseURL: process.env.SESSION_API_BASE_URL,
        headers: {
          ...prepareCrossServiceHeaders(requestSpan),
          cookie: `SessionId=${authResponse.SessionId}`,
        },
        search: {
          groupBy: ['createdAt'],
          dateTrunc: TimePrecision.DAY,
          createdAt: `gte:${addDays(new Date(), -30).toISOString()}`,
        },
      }
    );

    const countPageVisitsByDatePromise = PagesApi.count<CountByDateDataPoint[]>(
      {
        baseURL: process.env.SESSION_API_BASE_URL,
        headers: {
          ...prepareCrossServiceHeaders(requestSpan),
          cookie: `SessionId=${authResponse.SessionId}`,
        },
        search: {
          groupBy: ['createdAt'],
          dateTrunc: TimePrecision.DAY,
          createdAt: `gte:${addDays(new Date(), -30).toISOString()}`,
        },
      }
    );

    const countByLocationPromise = SessionApi.countByLocation({
      baseURL: process.env.SESSION_API_BASE_URL,
      headers: {
        ...prepareCrossServiceHeaders(requestSpan),
        cookie: `SessionId=${authResponse.SessionId}`,
      },
    }).then((data) => {
      return data.map((value) => ({
        ...value,
        'location.continentName': value['location.continentName'] || 'Unknown',
        'location.countryName': value['location.countryName'] || 'Unknown',
      }));
    });

    const countByDeviceClassPromise = SessionApi.countByDeviceClass({
      baseURL: process.env.SESSION_API_BASE_URL,
      headers: {
        ...prepareCrossServiceHeaders(requestSpan),
        cookie: `SessionId=${authResponse.SessionId}`,
      },
    });

    const [
      countSessionsByDate,
      countPageVisitsByDate,
      countByLocation,
      countByDeviceClass,
    ] = await Promise.all([
      countSessionsByDatePromise,
      countPageVisitsByDatePromise,
      countByLocationPromise,
      countByDeviceClassPromise,
    ]);

    return {
      props: {
        user: authResponse.user,
        organization: authResponse.organization,
        countByLocation,
        countByDeviceClass,
        countSessionsByDate,
        countPageVisitsByDate,
      },
    };
  } finally {
    requestSpan.finish();
  }
};

export default Home;
