import React from 'react';
import type { Session } from '@rebrowse/types';
import { AgentNameItem } from 'sessions/components/AgentNameItem';
import { OperatingSystemItem } from 'sessions/components/OperatingSystemItem';
import { DeviceClassItem } from 'sessions/components/DeviceClassItem';
import { Block } from 'baseui/block';
import { H5, Paragraph3 } from 'baseui/typography';
import { format } from 'date-fns';
import Divider from 'shared/components/Divider';
import { readableLocation } from 'shared/utils/location';
import { FaQuestion } from 'react-icons/fa';
import { SESSIONS_PAGE } from 'shared/constants/routes';
import { TermCondition } from '@rebrowse/sdk';

import { Item } from '../Item';

import { TagLink } from './TagLink';

type Props = {
  session: Session;
};

export const SessionDetails = ({
  session: { userAgent, createdAt, location },
}: Props) => {
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
          href={`${SESSIONS_PAGE}?user_agent.device_class=${TermCondition.EQ(
            userAgent.deviceClass
          )}`}
        >
          device.class = {userAgent.deviceClass}
        </TagLink>

        <TagLink
          href={`${SESSIONS_PAGE}?user_agent.device_brand=${TermCondition.EQ(
            userAgent.deviceBrand
          )}`}
        >
          device.brand = {userAgent.deviceBrand}
        </TagLink>
        <TagLink
          href={`${SESSIONS_PAGE}?user_agent.device_name=${TermCondition.EQ(
            userAgent.deviceName
          )}`}
        >
          device.name = {userAgent.deviceName}
        </TagLink>

        <TagLink
          href={`${SESSIONS_PAGE}?user_agent.agent_name=${TermCondition.EQ(
            userAgent.agentName
          )}&user_agent.agent_version=${TermCondition.EQ(
            userAgent.agentVersion
          )}`}
        >
          browser = {userAgent.agentName} {userAgent.agentVersion}
        </TagLink>
        <TagLink
          href={`${SESSIONS_PAGE}?user_agent.agent_name=${TermCondition.EQ(
            userAgent.agentName
          )}`}
        >
          browser.name = {userAgent.agentName}
        </TagLink>

        <TagLink
          href={`${SESSIONS_PAGE}?user_agent.operating_system_name=${TermCondition.EQ(
            userAgent.operatingSystemName
          )}&user_agent.operating_system_version=${TermCondition.EQ(
            userAgent.operatingSystemVersion
          )}`}
        >
          client_os = {userAgent.operatingSystemName}{' '}
          {userAgent.operatingSystemVersion}
        </TagLink>

        <TagLink
          href={`${SESSIONS_PAGE}?user_agent.operating_system_name=${TermCondition.EQ(
            userAgent.operatingSystemName
          )}`}
        >
          client_os.name = {userAgent.operatingSystemName}
        </TagLink>

        <TagLink
          href={`${SESSIONS_PAGE}?location.ip=${TermCondition.EQ(location.ip)}`}
        >
          user.ip = {location.ip}
        </TagLink>

        {location.continentName && (
          <TagLink
            href={`${SESSIONS_PAGE}?location.continent_name=${TermCondition.EQ(
              location.continentName
            )}`}
          >
            user.continent = {location.continentName}
          </TagLink>
        )}

        {location.countryName && (
          <TagLink
            href={`${SESSIONS_PAGE}?location.country_name=${TermCondition.EQ(
              location.countryName
            )}`}
          >
            user.country = {location.countryName}
          </TagLink>
        )}

        {location.regionName && (
          <TagLink
            href={`${SESSIONS_PAGE}?location.region_name=${TermCondition.EQ(
              location.regionName
            )}`}
          >
            user.region = {location.regionName}
          </TagLink>
        )}

        {location.city && (
          <TagLink
            href={`${SESSIONS_PAGE}?location.city=${TermCondition.EQ(
              location.city
            )}`}
          >
            user.city = {location.city}
          </TagLink>
        )}
      </Block>
    </Block>
  );
};
