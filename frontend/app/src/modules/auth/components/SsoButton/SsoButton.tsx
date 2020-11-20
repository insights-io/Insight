import React from 'react';
import { Theme } from 'baseui/theme';
import { UnstyledLink, Button } from '@rebrowse/elements';

type Props = {
  href: string;
  children: string;
  icon: React.ReactNode;
  theme: Theme;
};

export const SsoButton = ({ href, children, icon, theme }: Props) => {
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
