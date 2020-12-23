import React from 'react';
import type { Session } from '@rebrowse/types';
import { AgentNameItem } from 'sessions/components/AgentNameItem';
import { OperatingSystemItem } from 'sessions/components/OperatingSystemItem';
import { DeviceClassItem } from 'sessions/components/DeviceClassItem';
import { Block } from 'baseui/block';
import { H5, Paragraph3 } from 'baseui/typography';
import { format } from 'date-fns/esm';
import Divider from 'shared/components/Divider';
import { readableLocation } from 'shared/utils/location';
import { FaQuestion } from 'react-icons/fa';
import { SESSIONS_PAGE } from 'shared/constants/routes';

import { Item } from '../Item';

import { TagLink } from './TagLink';

type Props = {
  session: Session;
};

export const SessionDetails = ({
  session: { userAgent, createdAt, location },
}: Props) => {
  const browser = `${userAgent.agentName} ${userAgent.agentVersion}`;
  const operatingSystem = `${userAgent.operatingSystemName} ${userAgent.operatingSystemVersion}`;

  return (
    <Block>
      <Paragraph3>
        Started:{' '}
        <Block as="span" $style={{ fontWeight: 500 }}>
          {format(createdAt, 'MMM dd, YYY H:M:ss a')}
        </Block>
      </Paragraph3>

      <Paragraph3>
        Location:{' '}
        <Block as="span" $style={{ fontWeight: 500 }}>
          {readableLocation(location)}
        </Block>
      </Paragraph3>

      <Divider />
      <Block
        display="grid"
        gridTemplateColumns="repeat(auto-fit, minmax(200px, 1fr))"
        gridGap="16px"
      >
        <Item>
          <FaQuestion size={48} />
          <Item.Content>
            <Item.Title>{location.ip}</Item.Title>
          </Item.Content>
        </Item>

        <AgentNameItem
          agentName={userAgent.agentName}
          agentVersion={userAgent.agentVersion}
        />
        <OperatingSystemItem
          operatingSystemName={userAgent.operatingSystemName}
          operatingSystemVersion={userAgent.operatingSystemVersion}
        />
        <DeviceClassItem
          deviceClass={userAgent.deviceClass}
          deviceBrand={userAgent.deviceBrand}
          deviceName={userAgent.deviceName}
        />
      </Block>

      <Divider />

      <Block>
        <H5 margin={0} $style={{ fontSize: '18px' }}>
          Tags
        </H5>

        <TagLink
          href={`${SESSIONS_PAGE}?query=user_agent.device_class:eq${userAgent.deviceClass}`}
        >
          device.class = {userAgent.deviceClass}
        </TagLink>

        <TagLink
          href={`${SESSIONS_PAGE}?query=user_agent.device_brand:eq${userAgent.deviceBrand}`}
        >
          device.brand = {userAgent.deviceBrand}
        </TagLink>
        <TagLink
          href={`${SESSIONS_PAGE}?query=user_agent.device_name:eq${userAgent.deviceName}`}
        >
          device.name = {userAgent.deviceName}
        </TagLink>

        <TagLink
          href={`${SESSIONS_PAGE}?query=user_agent.browser:eq${browser}`}
        >
          browser = {browser}
        </TagLink>
        <TagLink
          href={`${SESSIONS_PAGE}?query=user_agent.browser_name:eq${userAgent.agentName}`}
        >
          browser.name = {userAgent.agentName}
        </TagLink>

        <TagLink
          href={`${SESSIONS_PAGE}?query=user_agent.operating_system:eq${operatingSystem}`}
        >
          client_os = {operatingSystem}
        </TagLink>

        <TagLink
          href={`${SESSIONS_PAGE}?query=user_agent.operating_system_name:eq${userAgent.operatingSystemName}`}
        >
          client_os.name = {userAgent.operatingSystemName}
        </TagLink>

        <TagLink href={`${SESSIONS_PAGE}?query=location.ip:eq${location.ip}`}>
          user.ip = {location.ip}
        </TagLink>

        {location.continentName && (
          <TagLink
            href={`${SESSIONS_PAGE}?query=location.continent_name:eq${location.continentName}`}
          >
            user.continent = {location.continentName}
          </TagLink>
        )}

        {location.countryName && (
          <TagLink
            href={`${SESSIONS_PAGE}?query=location.country_name:eq${location.countryName}`}
          >
            user.country = {location.countryName}
          </TagLink>
        )}

        {location.regionName && (
          <TagLink
            href={`${SESSIONS_PAGE}?query=location.region_name:eq${location.regionName}`}
          >
            user.region = {location.regionName}
          </TagLink>
        )}

        {location.city && (
          <TagLink
            href={`${SESSIONS_PAGE}?query=location.city:eq${location.city}`}
          >
            user.city = {location.city}
          </TagLink>
        )}
      </Block>
    </Block>
  );
};
