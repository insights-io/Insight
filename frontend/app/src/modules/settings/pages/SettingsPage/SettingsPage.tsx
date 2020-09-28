import AppLayout from 'modules/app/components/AppLayout';
import Link from 'next/link';
import React from 'react';
import {
  ACCOUNT_SETTINGS_DETAILS_PAGE,
  ACCOUNT_SETTINGS_SECURITY_PAGE,
  ORGANIZATION_SETTINGS_AUTH_PAGE,
  ORGANIZATION_SETTINGS_GENERAL_PAGE,
  ORGANIZATION_SETTINGS_MEMBERS_PAGE,
  SETTINGS_PATH_PART,
} from 'shared/constants/routes';
import { SettingsLayout } from 'modules/settings/components/SettingsLayout';
import { SETTINGS_SEARCH_OPTIONS } from 'modules/settings/constants';
import { Card } from 'baseui/card';
import Flex from 'shared/components/Flex';
import { Block } from 'baseui/block';
import FlexColumn from 'shared/components/FlexColumn';
import type { Path } from 'modules/settings/types';
import { Avatar } from 'baseui/avatar';
import Divider from 'shared/components/Divider';
import { OrganizationDTO, UserDTO } from '@insight/types';
import { useUser } from 'shared/hooks/useUser';
import useOrganization from 'shared/hooks/useOrganization';

const PATH: Path = [SETTINGS_PATH_PART];

type Props = {
  user: UserDTO;
  organization: OrganizationDTO;
};

export const SettingsPage = ({
  user: initialUser,
  organization: initialOrganization,
}: Props) => {
  const { user } = useUser(initialUser);
  const { organization } = useOrganization(initialOrganization);

  return (
    <AppLayout>
      <SettingsLayout searchOptions={SETTINGS_SEARCH_OPTIONS} path={PATH}>
        <Flex flexWrap>
          <Card
            overrides={{
              Root: { style: { width: '100%', maxWidth: '350px' } },
            }}
          >
            <Flex justifyContent="center">
              <Link href={ACCOUNT_SETTINGS_DETAILS_PAGE}>
                <a>
                  <FlexColumn>
                    <Flex justifyContent="center">
                      <Avatar name={user.fullName} />
                    </Flex>
                    <Block marginTop="12px">My account</Block>
                  </FlexColumn>
                </a>
              </Link>
            </Flex>

            <Divider />

            <FlexColumn>
              <Block>Quick links:</Block>
              <ul>
                <li>
                  <Link href={ACCOUNT_SETTINGS_SECURITY_PAGE}>
                    <a>Change my password</a>
                  </Link>
                </li>
                <li>
                  <Link href={ACCOUNT_SETTINGS_SECURITY_PAGE}>
                    <a>Setup Two-Factor Authentication</a>
                  </Link>
                </li>
              </ul>
            </FlexColumn>
          </Card>

          <Card
            overrides={{
              Root: { style: { width: '100%', maxWidth: '350px' } },
            }}
          >
            <Flex justifyContent="center">
              <Link href={ORGANIZATION_SETTINGS_GENERAL_PAGE}>
                <a>
                  <FlexColumn>
                    <Flex justifyContent="center">
                      <Avatar name={organization.name || 'O'} />
                    </Flex>
                    <Block marginTop="12px">
                      {organization.name || 'Organization'}
                    </Block>
                  </FlexColumn>
                </a>
              </Link>
            </Flex>
            <Divider />

            <FlexColumn>
              <Block>Quick links:</Block>
              <ul>
                <li>
                  <Link href={ORGANIZATION_SETTINGS_MEMBERS_PAGE}>
                    <a>Members</a>
                  </Link>
                </li>
                <li>
                  <Link href={ORGANIZATION_SETTINGS_AUTH_PAGE}>
                    <a>Setup authentication</a>
                  </Link>
                </li>
              </ul>
            </FlexColumn>
          </Card>
        </Flex>
      </SettingsLayout>
    </AppLayout>
  );
};
