import { stringify } from 'querystring';

import type { GetServerSideProps } from 'next';
import { SIGNIN_ROUTE } from 'shared/constants/routes';

export default function Index() {
  return null;
}

export const getServerSideProps: GetServerSideProps = async (ctx) => {
  let destination = SIGNIN_ROUTE;
  if (Object.keys(ctx.query).length > 0) {
    destination = `${destination}?${stringify(ctx.query)}`;
  }

  return {
    redirect: {
      destination,
      permanent: true,
    },
  };
};
