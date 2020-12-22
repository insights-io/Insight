import React from 'react';
import { configureStory, fullHeightDecorator } from '@rebrowse/storybook';
import { useStyletron } from 'baseui';
import { Block } from 'baseui/block';
import {
  REBROWSE_ADMIN,
  NAMELESS_ADMIN,
  REBROWSE_ORGANIZATION,
} from '__tests__/data';
import type { Meta } from '@storybook/react';
import { Theme } from 'baseui/theme';
import { AuthApi } from 'api';

import { NavbarBanner } from './Banner';

export default {
  title: 'app/components/NavbarBanner',
  component: NavbarBanner,
  decorators: [fullHeightDecorator],
} as Meta;

type WrapperProps = {
  children: React.ReactNode;
  theme: Theme;
};

const Wrapper = ({ theme, children }: WrapperProps) => {
  return (
    <Block
      width="fit-content"
      backgroundColor={theme.colors.primary}
      color={theme.colors.white}
      padding={theme.sizing.scale600}
    >
      {children}
    </Block>
  );
};

export const Base = () => {
  const [_css, theme] = useStyletron();
  return (
    <Wrapper theme={theme}>
      <NavbarBanner
        expanded
        organizationName={REBROWSE_ORGANIZATION.name}
        organizationAvatar={undefined}
        user={REBROWSE_ADMIN}
        theme={theme}
      />
    </Wrapper>
  );
};
Base.story = configureStory({
  setupMocks: (sandbox) => {
    return sandbox.stub(AuthApi.sso.session, 'logout').resolves();
  },
});

export const NamelessUserAndOrganization = () => {
  const [_css, theme] = useStyletron();
  return (
    <Wrapper theme={theme}>
      <NavbarBanner
        expanded
        organizationName={undefined}
        organizationAvatar={undefined}
        user={NAMELESS_ADMIN}
        theme={theme}
      />
    </Wrapper>
  );
};
NamelessUserAndOrganization.story = configureStory({
  setupMocks: (sandbox) => {
    return sandbox.stub(AuthApi.sso.session, 'logout').resolves();
  },
});
