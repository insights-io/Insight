import React from 'react';
import { authenticated } from 'modules/auth/middleware/authMiddleware';
import { GetServerSideProps, GetServerSidePropsResult } from 'next';
import { startRequestSpan, prepareCrossServiceHeaders } from 'modules/tracing';
import {
  InsightsPage,
  CountByDateDataPoint,
  CountByDeviceClassDataPoint,
  InsightsPageProps,
  CountByLocationDataPoint,
} from 'modules/insights/pages/InsightsPage';
import { SessionApi, PagesApi } from 'api';
import { TimePrecision } from '@rebrowse/types';
import { addDays } from 'date-fns';

export type Props = InsightsPageProps;

const Home = ({
  user,
  countSessionsByLocation,
  organization,
  countSessionsByDeviceClass,
  countSessionsByDate,
  countPageVisitsByDate,
}: Props) => {
  return (
    <InsightsPage
      user={user}
      countSessionsByLocation={countSessionsByLocation}
      countSessionsByDeviceClass={countSessionsByDeviceClass}
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

    const createdAtGte = `gte:${addDays(new Date(), -30).toISOString()}`;
    const headers = {
      ...prepareCrossServiceHeaders(requestSpan),
      cookie: `SessionId=${authResponse.SessionId}`,
    };

    const countSessionsByDatePromise = SessionApi.count<CountByDateDataPoint[]>(
      {
        baseURL: process.env.SESSION_API_BASE_URL,
        headers,
        search: {
          groupBy: ['createdAt'],
          dateTrunc: TimePrecision.DAY,
          createdAt: createdAtGte,
        },
      }
    );

    const countSessionsByDeviceClassPromise = SessionApi.count<
      CountByDeviceClassDataPoint[]
    >({
      baseURL: process.env.SESSION_API_BASE_URL,
      headers,
      search: { groupBy: ['userAgent.deviceClass'], createdAt: createdAtGte },
    });

    const countSessionsByLocationPromise = SessionApi.count<
      CountByLocationDataPoint[]
    >({
      baseURL: process.env.SESSION_API_BASE_URL,
      headers,
      search: {
        groupBy: ['location.countryName', 'location.continentName'],
        createdAt: createdAtGte,
      },
    });

    const countPageVisitsByDatePromise = PagesApi.count<CountByDateDataPoint[]>(
      {
        baseURL: process.env.SESSION_API_BASE_URL,
        headers,
        search: {
          groupBy: ['createdAt'],
          dateTrunc: TimePrecision.DAY,
          createdAt: createdAtGte,
        },
      }
    );

    const [
      countSessionsByDate,
      countPageVisitsByDate,
      countSessionsByLocation,
      countSessionsByDeviceClass,
    ] = await Promise.all([
      countSessionsByDatePromise,
      countPageVisitsByDatePromise,
      countSessionsByLocationPromise,
      countSessionsByDeviceClassPromise,
    ]);

    return {
      props: {
        user: authResponse.user,
        organization: authResponse.organization,
        countSessionsByLocation,
        countSessionsByDeviceClass,
        countSessionsByDate,
        countPageVisitsByDate,
      },
    };
  } finally {
    requestSpan.finish();
  }
};

export default Home;
