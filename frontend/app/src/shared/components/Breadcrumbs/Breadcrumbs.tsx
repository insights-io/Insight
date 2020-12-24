import React from 'react';
import { Breadcrumbs as BasewebBreadcrumbs } from 'baseui/breadcrumbs';
import Link from 'next/link';
import { UnstyledLink } from '@rebrowse/elements';

import { Path } from './types';
import { joinSegments } from './utils';

type Props = {
  path: Path;
};

export const Breadcrumbs = React.memo(({ path }: Props) => {
  return (
    <BasewebBreadcrumbs
      overrides={{
        List: { style: { display: 'flex', flexWrap: 'wrap' } },
        ListItem: { style: { display: 'flex' } },
        Separator: {
          style: {
            display: 'flex',
            flexDirection: 'column',
            justifyContent: 'center',
          },
        },
      }}
    >
      {path.map((pathPart, index) => {
        const link = joinSegments(
          path.slice(0, index + 1).map((p) => p.segment)
        );

        return (
          <Link key={link} href={link}>
            <UnstyledLink href={link}>{pathPart.text}</UnstyledLink>
          </Link>
        );
      })}
    </BasewebBreadcrumbs>
  );
});
