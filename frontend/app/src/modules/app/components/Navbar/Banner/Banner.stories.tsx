import React from 'react';
import { fullHeightDecorator } from '@insight/storybook';
import { useStyletron } from 'baseui';
import { Block } from 'baseui/block';
import { INSIGHT_ADMIN } from 'test/data';

import { NavbarBanner } from './Banner';

export default {
  title: 'app/components/NavbarBanner',
  decorators: [fullHeightDecorator],
};

export const Base = () => {
  const [_css, theme] = useStyletron();
  return (
    <Block
      width="fit-content"
      backgroundColor={theme.colors.primary}
      color={theme.colors.white}
      padding={theme.sizing.scale600}
    >
      <NavbarBanner
        expanded
        organizationName="Insight"
        user={INSIGHT_ADMIN}
        theme={theme}
      />
    </Block>
  );
};
