import React from 'react';
import { UserAgentDTO } from '@insight/types';

type Props = {
  value: UserAgentDTO;
};

const UserAgent = ({ value }: Props) => {
  return (
    <>
      {value.operatingSystemName} &bull; {value.browserName}
    </>
  );
};

export default UserAgent;
