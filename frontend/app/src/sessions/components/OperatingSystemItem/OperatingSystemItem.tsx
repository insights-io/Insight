import React from 'react';
import { UserAgentDTO } from '@rebrowse/types';
import { Block } from 'baseui/block';
import { getOperatingSystemIcon } from 'sessions/utils/user-agent';

import { Item } from '../Item';
import { ItemProps } from '../Item/Item';

type Props = ItemProps &
  Pick<UserAgentDTO, 'operatingSystemName' | 'operatingSystemVersion'>;

export const OperatingSystemItem = ({
  operatingSystemName,
  operatingSystemVersion,
  ...itemProps
}: Props) => {
  const IconComponent = getOperatingSystemIcon(operatingSystemName);

  return (
    <Item {...itemProps}>
      <IconComponent size={48} />
      <Item.Content>
        <Item.Title>{operatingSystemName}</Item.Title>
        <Item.Subtitle>
          Version:{' '}
          <Block as="span" $style={{ fontWeight: 500 }}>
            {operatingSystemVersion}
          </Block>
        </Item.Subtitle>
      </Item.Content>
    </Item>
  );
};
