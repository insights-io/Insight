import { User } from '@insight/types';
import React from 'react';

import TwoFactorAuthentication from './TwoFactorAuthentication';

type Props = {
  user: User;
};

const Security = ({ user }: Props) => {
  return <TwoFactorAuthentication user={user} />;
};

export default Security;
