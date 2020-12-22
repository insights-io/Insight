import React from 'react';
import { UserAgentDTO } from '@rebrowse/types';

type Props = {
  value: UserAgentDTO;
};

export const UserAgent = ({ value }: Props) => {
  return (
    <>
      {value.operatingSystemName} &bull; {value.agentName}
    </>
  );
};

export default UserAgent;
