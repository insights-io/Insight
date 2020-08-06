import React from 'react';
import {
  AuthenticatedServerSideProps,
  authenticated,
} from 'modules/auth/middleware/authMiddleware';
import { GetServerSideProps, GetServerSidePropsResult } from 'next';
import { startRequestSpan, prepareCrossServiceHeaders } from 'modules/tracing';
import InsightsPage from 'modules/insights/pages/InsightsPage';
import { mapUser } from '@insight/sdk';
import { SessionApi } from 'api';

type Props = AuthenticatedServerSideProps & {
  countByCountry: Record<string, number>;
  countByDeviceClass: Record<string, number>;
  countByContinent: Record<string, number>;
};

const Home = ({
  user: initialUser,
  countByCountry,
  countByDeviceClass,
  countByContinent,
}: Props) => {
  return (
    <InsightsPage
      user={mapUser(initialUser)}
      countByCountry={countByCountry}
      countByDeviceClass={countByDeviceClass}
      countByContinent={countByContinent}
    />
  );
};

export const getServerSideProps: GetServerSideProps<Props> = async (
  context
) => {
  const requestSpan = startRequestSpan(context.req);
  const authResponse = await authenticated(context, requestSpan);
  if (!authResponse) {
    return ({ props: {} } as unknown) as GetServerSidePropsResult<Props>;
  }

  const countByLocationPromise = SessionApi.countByLocation({
    baseURL: process.env.SESSION_API_BASE_URL,
    headers: {
      ...prepareCrossServiceHeaders(requestSpan),
      cookie: `SessionId=${authResponse.SessionId}`,
    },
  });

  const countByDeviceClassPromise = SessionApi.countByDeviceClass({
    baseURL: process.env.SESSION_API_BASE_URL,
    headers: {
      ...prepareCrossServiceHeaders(requestSpan),
      cookie: `SessionId=${authResponse.SessionId}`,
    },
  });

  const [countByLocation, countByDeviceClass] = await Promise.all([
    countByLocationPromise,
    countByDeviceClassPromise,
  ]);

  const countByCountry = countByLocation.reduce((acc, entry) => {
    return { ...acc, [entry['location.countryName']]: entry.count };
  }, {} as Record<string, number>);

  const countByContinent = countByLocation.reduce((acc, entry) => {
    const key = entry['location.continentName'];
    const count = (acc[key] || 0) + entry.count;
    return { ...acc, [key]: count };
  }, {} as Record<string, number>);

  return {
    props: {
      user: authResponse.user,
      countByCountry,
      countByContinent,
      countByDeviceClass,
    },
  };
};

export default Home;
