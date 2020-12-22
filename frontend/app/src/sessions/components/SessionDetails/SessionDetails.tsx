import React from 'react';
import { H3 } from 'baseui/typography';
import { Block } from 'baseui/block';
import type { Session } from '@rebrowse/types';
import { Flex, FlexColumn } from '@rebrowse/elements';

import { AgentNameItem } from '../AgentNameItem';
import { OperatingSystemItem } from '../OperatingSystemItem';
import { DeviceClassItem } from '../DeviceClassItem/DeviceClassItem';

type Props = {
  session: Session;
};

export const SessionDetails = ({ session }: Props) => {
  return (
    <>
      <Block display="flex" height="100%">
        <FlexColumn overflow="hidden" width="100%">
          <H3>Session {session.id}</H3>
          <div>Device ID: {session.deviceId}</div>
        </FlexColumn>
      </Block>
      <Flex>
        <AgentNameItem
          agentName={session.userAgent.agentName}
          agentVersion={session.userAgent.agentVersion}
        />
        <OperatingSystemItem
          marginLeft="24px"
          operatingSystemName={session.userAgent.operatingSystemName}
          operatingSystemVersion={session.userAgent.operatingSystemVersion}
        />
        <DeviceClassItem
          marginLeft="24px"
          deviceClass={session.userAgent.deviceClass}
        />
      </Flex>
    </>
  );
};
