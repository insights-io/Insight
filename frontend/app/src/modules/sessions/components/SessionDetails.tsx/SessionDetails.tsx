import React from 'react';
import { H3 } from 'baseui/typography';
import { Block } from 'baseui/block';
import { Session } from '@insight/types';
import DevTools from 'modules/sessions/containers/DevTools';

type Props = {
  session: Session;
};

const SessionDetails = ({ session }: Props) => {
  return (
    <>
      <Block display="flex" height="100%">
        <Block
          display="flex"
          flexDirection="column"
          overflow="hidden"
          width="100%"
        >
          <H3>Session {session.id}</H3>
          <div>Device ID: {session.deviceId}</div>
        </Block>
      </Block>
      <DevTools sessionId={session.id} />
    </>
  );
};

export default SessionDetails;
