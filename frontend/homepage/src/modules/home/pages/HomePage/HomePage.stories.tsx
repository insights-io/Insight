import React from 'react';

import HomePage from './HomePage';

export default {
  title: 'home/pages/HomePage',
};

export const LoggedIn = () => {
  return <HomePage loggedIn />;
};

export const NotLoggedIn = () => {
  return <HomePage loggedIn={false} />;
};
