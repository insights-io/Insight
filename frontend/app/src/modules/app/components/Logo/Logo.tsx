import React from 'react';
import Link from 'next/link';
import { Upload } from 'baseui/icon';
import { Button, SHAPE } from 'baseui/button';

const Logo = () => {
  return (
    <Link href="/">
      <a style={{ color: 'inherit' }} tabIndex={-1}>
        <Button shape={SHAPE.round} size="mini">
          <Upload />
        </Button>
      </a>
    </Link>
  );
};

export default React.memo(Logo);
