import React from 'react';
import { UserAgentDTO } from '@rebrowse/types';
import { Block } from 'baseui/block';
import { getAgentNameIcon } from 'sessions/utils/user-agent';

import { Item } from '../Item';

type Props = Pick<UserAgentDTO, 'agentName' | 'agentVersion'>;

export const AgentNameItem = ({ agentName, agentVersion }: Props) => {
  const IconComponent = getAgentNameIcon(agentName);

  return (
    <Item>
      <IconComponent size={48} />
      <Item.Content>
        <Item.Title>{agentName}</Item.Title>
        <Item.Subtitle>
          Version:{' '}
          <Block as="span" $style={{ fontWeight: 500 }}>
            {agentVersion}
          </Block>
        </Item.Subtitle>
      </Item.Content>
    </Item>
  );
};
