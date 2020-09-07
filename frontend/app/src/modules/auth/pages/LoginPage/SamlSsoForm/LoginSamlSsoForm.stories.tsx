import React from 'react';

import LoginSamlSsoForm from './LoginSamlSsoForm';

export default {
  title: 'Auth/components/LoginSamlSsoForm',
};

export const Base = () => {
  return <LoginSamlSsoForm encodedRedirect={encodeURIComponent('/')} />;
};
