import React from 'react';
import { Card } from 'baseui/card';
import { Flex, FlexColumn } from '@insight/elements';
import Link from 'next/link';
import { Avatar } from 'baseui/avatar';
import { Block } from 'baseui/block';
import Divider from 'shared/components/Divider';
import { useStyletron } from 'baseui';

type Props = {
  header: string;
  avatar: string;
  headerLink: string;
  quickLinks: { text: string; link: string }[];
};

export const SettingsSectionCard = ({
  header,
  avatar,
  headerLink,
  quickLinks,
}: Props) => {
  const [css, theme] = useStyletron();
  const linkStyles = css({ textDecoration: 'none' });

  return (
    <Card
      overrides={{
        Root: {
          style: {
            width: '100%',
            maxWidth: '350px',
            borderRadius: '8px',
          },
        },
      }}
    >
      <Flex justifyContent="center">
        <Link href={headerLink}>
          <a className={linkStyles}>
            <FlexColumn>
              <Flex justifyContent="center">
                <Avatar
                  name={avatar}
                  overrides={{
                    Root: { style: { background: theme.colors.accent700 } },
                  }}
                />
              </Flex>
              <Block marginTop={theme.sizing.scale400}>{header}</Block>
            </FlexColumn>
          </a>
        </Link>
      </Flex>

      <Divider />

      <FlexColumn>
        <Block $style={{ fontWeight: 500, fontSize: '0.9rem' }}>
          Quick links:
        </Block>

        <ul>
          {quickLinks.map(({ link, text }) => {
            return (
              <li key={link}>
                <Link href={link}>
                  <a className={linkStyles}>{text}</a>
                </Link>
              </li>
            );
          })}
        </ul>
      </FlexColumn>
    </Card>
  );
};
