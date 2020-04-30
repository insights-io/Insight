import React from 'react';
import authenticated from 'modules/auth/hoc/authenticated';
import AppLayout from 'modules/app/components/AppLayout';

const Home = () => {
  /*
  useEffect(() => {
    console.log('HELLO WORLD');
    const exampleSocket = new WebSocket('ws://127.0.0.1:8082/v1/sessions');

    exampleSocket.onopen = (event) => {
      console.log({ event });
    };

    exampleSocket.onmessage = (event) => {
      console.log({ event });
    };

    return () => {
      exampleSocket.close();
    };
  }, []);
  */

  return <AppLayout>TODO</AppLayout>;
};

export default authenticated(Home);
