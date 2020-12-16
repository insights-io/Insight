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
import { SessionApi } from 'api';
import { CountByLocation } from 'modules/insights/components/charts/CountByLocationMapChart/utils';
import { TimePrecision } from '@rebrowse/types';
import { addDays } from 'date-fns';

type Props = AuthenticatedServerSideProps & {
  countByLocation: CountByLocation;
  countByDeviceClass: Record<string, number>;
  countByDate: CountByDateDataPoint[];
};

const Home = ({
  user,
  countByLocation,
  organization,
  countByDeviceClass,
  countByDate,
}: Props) => {
  return (
    <InsightsPage
      user={user}
      countByLocation={countByLocation}
      countByDeviceClass={countByDeviceClass}
      organization={organization}
      countByDate={countByDate}
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

    const countByDatePromise = SessionApi.count<CountByDateDataPoint[]>({
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
    });

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
      countByDate,
      countByLocation,
      countByDeviceClass,
    ] = await Promise.all([
      countByDatePromise,
      countByLocationPromise,
      countByDeviceClassPromise,
    ]);

    return {
      props: {
        user: authResponse.user,
        organization: authResponse.organization,
        countByLocation,
        countByDeviceClass,
        countByDate,
      },
    };
  } finally {
    requestSpan.finish();
  }
};

export default Home;
