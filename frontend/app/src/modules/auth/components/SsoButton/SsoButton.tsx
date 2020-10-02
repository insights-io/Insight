import React from 'react';
import { Button } from 'baseui/button';
import { Theme } from 'baseui/theme';
import { UnstyledLink } from '@insight/elements';

type Props = {
  href: string;
  children: string;
  icon: React.ReactNode;
  theme: Theme;
};

const SsoButton = ({ href, children, icon, theme }: Props) => {
  return (
    <UnstyledLink href={href}>
      <Button
        $style={{ width: '100%', marginTop: theme.sizing.scale200 }}
        startEnhancer={icon}
        kind="secondary"
      >
        {children}
      </Button>
    </UnstyledLink>
  );
};

export default SsoButton;
