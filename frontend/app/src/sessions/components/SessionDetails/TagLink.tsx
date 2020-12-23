import React from 'react';
import { UnstyledLink } from '@rebrowse/elements';
import { Tag, TagProps } from 'baseui/tag';
import Link from 'next/link';

type Props = TagProps & {
  href: string;
};

export const TagLink = ({ href, ...tagProps }: Props) => {
  return (
    <Link href={href}>
      <UnstyledLink href={href}>
        <Tag
          variant="outlined"
          kind="neutral"
          closeable={false}
          overrides={{
            Text: { style: { maxWidth: 'auto' } },
            Root: { style: { cursor: 'pointer' } },
          }}
          {...tagProps}
        />
      </UnstyledLink>
    </Link>
  );
};
