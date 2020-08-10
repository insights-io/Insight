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
import { CountByLocation } from 'modules/insights/components/charts/CountByLocationMapChart/utils';

type Props = AuthenticatedServerSideProps & {
  countByLocation: CountByLocation;
  countByDeviceClass: Record<string, number>;
};

const Home = ({
  user: initialUser,
  countByLocation,
  countByDeviceClass,
}: Props) => {
  return (
    <InsightsPage
      user={mapUser(initialUser)}
      countByLocation={countByLocation}
      countByDeviceClass={countByDeviceClass}
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

    return {
      props: {
        user: authResponse.user,
        countByLocation,
        countByDeviceClass,
      },
    };
  } finally {
    requestSpan.finish();
  }
};

export default Home;
