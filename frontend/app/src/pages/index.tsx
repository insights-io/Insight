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
};

const Home = ({
  user: initialUser,
  countByCountry,
  countByDeviceClass,
}: Props) => {
  return (
    <InsightsPage
      user={mapUser(initialUser)}
      countByCountry={countByCountry}
      countByDeviceClass={countByDeviceClass}
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

  const countByCountryPromise = SessionApi.countByCountries({
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

  const [countByCountry, countByDeviceClass] = await Promise.all([
    countByCountryPromise,
    countByDeviceClassPromise,
  ]);

  return {
    props: { user: authResponse.user, countByCountry, countByDeviceClass },
  };
};

export default Home;
