import React from 'react';
import Link from 'next/link';
import { Button, SHAPE } from 'baseui/button';
import { useStyletron } from 'baseui';

const Logo = () => {
  const [css, theme] = useStyletron();
  return (
    <Link href="/">
      <a style={{ color: 'inherit' }} tabIndex={-1}>
        <Button
          shape={SHAPE.round}
          size="mini"
          $style={{
            ':hover': { backgroundColor: theme.colors.black },
            ':focus': { backgroundColor: theme.colors.black },
          }}
        >
          <img
            src="/static/icons/icon-72x72.png"
            alt="Logo"
            className={css({
              width: '32px',
              ':hover': { transform: 'scale(1.25)' },
            })}
          />
        </Button>
      </a>
    </Link>
  );
};

export default React.memo(Logo);
