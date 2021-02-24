import React from 'react';
import { GetServerSideProps } from 'next';
import { client } from 'sdk';
import { HomePage } from 'landing/pages/HomePage';

type Props = {
  loggedIn: boolean;
};

export default function Index({ loggedIn }: Props) {
  return <HomePage loggedIn={loggedIn} />;
}

export const getServerSideProps: GetServerSideProps<Props> = async (ctx) => {
  const { SessionId } = ctx.req.cookies || {};
  if (!SessionId) {
    return { props: { loggedIn: false } };
  }
  const response = await client.retrieve(SessionId);
  return { props: { loggedIn: response.statusCode === 200 } };
};
