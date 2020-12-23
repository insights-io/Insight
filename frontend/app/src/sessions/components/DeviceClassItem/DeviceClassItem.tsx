import React from 'react';
import { UserAgentDTO } from '@rebrowse/types';
import { getDeviceClassIcon } from 'sessions/utils/user-agent';
import { Block } from 'baseui/block';

import { Item, ItemProps } from '../Item';

type Props = Pick<UserAgentDTO, 'deviceClass' | 'deviceBrand' | 'deviceName'> &
  ItemProps;

export const DeviceClassItem = ({
  deviceClass,
  deviceBrand,
  deviceName,
  ...itemProps
}: Props) => {
  const IconComponent = getDeviceClassIcon(deviceClass);

  return (
    <Item {...itemProps}>
      <IconComponent size={48} />
      <Item.Content>
        <Item.Title>{deviceClass}</Item.Title>
        <Item.Subtitle>
          {deviceBrand}:{' '}
          <Block as="span" $style={{ fontWeight: 500 }}>
            {deviceName}
          </Block>
        </Item.Subtitle>
      </Item.Content>
    </Item>
  );
};
