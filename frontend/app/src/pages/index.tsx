import React from 'react';
import { authenticated } from 'modules/auth/middleware/authMiddleware';
import type { GetServerSideProps, GetServerSidePropsResult } from 'next';
import {
  startRequestSpan,
  prepareCrossServiceHeaders,
} from 'shared/utils/tracing';
import { InsightsPage, InsightsPageProps } from 'insights/pages/InsightsPage';
import { RelativeTimeRange, timeRelative } from 'shared/utils/date';
import {
  countSessionsByDeviceClass,
  countSessionsByLocation,
  countSessionsByDate,
  countPageVisitsByDate,
} from 'insights/api';

export type Props = InsightsPageProps;

const Home = ({
  user,
  organization,
  relativeTimeRange,
  sessionsByLocationCount,
  sessionsByDeviceCount,
  sessionsByDateCount,
  pageVisitsByDateCount,
}: Props) => {
  return (
    <InsightsPage
      user={user}
      organization={organization}
      relativeTimeRange={relativeTimeRange}
      sessionsByLocationCount={sessionsByLocationCount}
      sessionsByDeviceCount={sessionsByDeviceCount}
      sessionsByDateCount={sessionsByDateCount}
      pageVisitsByDateCount={pageVisitsByDateCount}
    />
  );
};

export const getServerSideProps: GetServerSideProps<Props> = async (
  context
) => {
  const relativeTimeRange: RelativeTimeRange = '30d';
  const requestSpan = startRequestSpan(context.req);
  try {
    const authResponse = await authenticated(context, requestSpan);
    if (!authResponse) {
      return ({ props: {} } as unknown) as GetServerSidePropsResult<Props>;
    }

    const createdAtGte = `gte:${timeRelative(relativeTimeRange).toISOString()}`;
    const headers = {
      ...prepareCrossServiceHeaders(requestSpan),
      cookie: `SessionId=${authResponse.SessionId}`,
    };

    const countSessionsByDatePromise = countSessionsByDate(createdAtGte, {
      baseURL: process.env.SESSION_API_BASE_URL,
      headers,
    });

    const countSessionsByDeviceClassPromise = countSessionsByDeviceClass(
      createdAtGte,
      { baseURL: process.env.SESSION_API_BASE_URL, headers }
    );

    const countSessionsByLocationPromise = countSessionsByLocation(
      createdAtGte,
      { baseURL: process.env.SESSION_API_BASE_URL, headers }
    );

    const countPageVisitsByDatePromise = countPageVisitsByDate(createdAtGte, {
      baseURL: process.env.SESSION_API_BASE_URL,
      headers,
    });

    const [
      sessionsByDateCount,
      sessionsByLocationCount,
      sessionsByDeviceCount,
      pageVisitsByDateCount,
    ] = await Promise.all([
      countSessionsByDatePromise,
      countSessionsByLocationPromise,
      countSessionsByDeviceClassPromise,
      countPageVisitsByDatePromise,
    ]);

    return {
      props: {
        user: authResponse.user,
        organization: authResponse.organization,
        sessionsByLocationCount,
        sessionsByDeviceCount,
        sessionsByDateCount,
        pageVisitsByDateCount,
        relativeTimeRange,
      },
    };
  } finally {
    requestSpan.finish();
  }
};

export default Home;
