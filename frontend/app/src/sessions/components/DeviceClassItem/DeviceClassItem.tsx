import React from 'react';
import { UserAgentDTO } from '@rebrowse/types';
import { getDeviceClassIcon } from 'sessions/utils/user-agent';

import { Item, ItemProps } from '../Item';

type Props = Pick<UserAgentDTO, 'deviceClass'> & ItemProps;

export const DeviceClassItem = ({ deviceClass, ...itemProps }: Props) => {
  const IconComponent = getDeviceClassIcon(deviceClass);

  return (
    <Item {...itemProps}>
      <IconComponent size={48} />
      <Item.Content>
        <Item.Title>{deviceClass}</Item.Title>
      </Item.Content>
    </Item>
  );
};
