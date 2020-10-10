import Router from 'next/router';
import { appBaseURL } from 'shared/config';
import type { GetServerSideProps } from 'next';

// eslint-disable-next-line lodash/prefer-constant
const SignUpCompletedCallback = () => null;

export const getServerSideProps: GetServerSideProps = async (ctx) => {
  const Location = appBaseURL;
  if (ctx.res) {
    ctx.res.writeHead(302, { Location });
    ctx.res.end();
  } else {
    Router.push(Location);
  }
  return { props: {} };
};

export default SignUpCompletedCallback;
