import React from 'react';
import { Avatar, AvatarProps } from 'baseui/avatar';
import { Organization, UserDTO } from '@rebrowse/types';
import { useStyletron } from 'baseui';
import { expandBorderRadius } from '@rebrowse/elements';

type Props = Pick<Organization, 'name' | 'avatar'> &
  Omit<AvatarProps, 'name' | 'src'>;

export const OrganizationAvatar = ({ name, avatar, ...rest }: Props) => {
  const [_css, theme] = useStyletron();
  const src = avatar?.type === 'avatar' ? avatar.image : undefined;
  const borderRadius = expandBorderRadius(theme.sizing.scale500);

  return (
    <Avatar
      name={name || 'O'}
      src={src}
      overrides={{
        Avatar: {
          style: borderRadius,
          props: { alt: 'Avatar' },
        },
        Root: {
          style: {
            backgroundColor: src ? undefined : theme.colors.accent600,
            ...borderRadius,
          },
        },
      }}
      {...rest}
    />
  );
};

type UserAvatarProps = Pick<UserDTO, 'email' | 'fullName'> &
  Omit<AvatarProps, 'name' | 'src'>;

export const UserAvatar = ({ fullName, email, ...rest }: UserAvatarProps) => {
  return <Avatar name={fullName || email} {...rest} />;
};
