import React from 'react';
import {
  AuthenticatedServerSideProps,
  authenticated,
} from 'modules/auth/middleware/authMiddleware';
import { GetServerSideProps, GetServerSidePropsResult } from 'next';
import { startRequestSpan, prepareCrossServiceHeaders } from 'modules/tracing';
import { InsightsPage } from 'modules/insights/pages/InsightsPage';
import { SessionApi } from 'api';
import { CountByLocation } from 'modules/insights/components/charts/CountByLocationMapChart/utils';

type Props = AuthenticatedServerSideProps & {
  countByLocation: CountByLocation;
  countByDeviceClass: Record<string, number>;
};

const Home = ({
  user,
  countByLocation,
  organization,
  countByDeviceClass,
}: Props) => {
  return (
    <InsightsPage
      user={user}
      countByLocation={countByLocation}
      countByDeviceClass={countByDeviceClass}
      organization={organization}
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

    const [countByLocation, countByDeviceClass] = await Promise.all([
      countByLocationPromise,
      countByDeviceClassPromise,
    ]);

    return {
      props: {
        user: authResponse.user,
        organization: authResponse.organization,
        countByLocation,
        countByDeviceClass,
      },
    };
  } finally {
    requestSpan.finish();
  }
};

export default Home;
