import React from 'react';
import { GetServerSideProps } from 'next';
import { sdk } from 'api';
import { HomePage } from 'landing/pages/HomePage';

type Props = {
  loggedIn: boolean;
};

const Home = ({ loggedIn }: Props) => {
  return <HomePage loggedIn={loggedIn} />;
};

export const getServerSideProps: GetServerSideProps<Props> = async (ctx) => {
  const { SessionId } = ctx.req.cookies || {};
  if (!SessionId) {
    return { props: { loggedIn: false } };
  }
  const response = await sdk.retrieve(SessionId);
  return { props: { loggedIn: response.status === 200 } };
};

export default Home;
