import React from 'react';
import { GetServerSideProps } from 'next';
import nextCookie from 'next-cookies';
import SsoSessionApi from 'api';
import HomePage from 'modules/home/pages/HomePage';

type Props = {
  loggedIn: boolean;
};

const Home = ({ loggedIn }: Props) => {
  return <HomePage loggedIn={loggedIn} />;
};

export const getServerSideProps: GetServerSideProps<Props> = async (ctx) => {
  const { SessionId } = nextCookie(ctx);
  if (!SessionId) {
    return { props: { loggedIn: false } };
  }

  const response = await SsoSessionApi.get(SessionId, {
    baseURL: process.env.AUTH_API_BASE_URL,
  });
  return { props: { loggedIn: response.status === 200 } };
};

export default Home;
