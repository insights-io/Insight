/* eslint-disable jsx-a11y/click-events-have-key-events */
/* eslint-disable jsx-a11y/no-noninteractive-element-interactions */
import React, { useEffect, useCallback } from 'react';
import { useStyletron } from 'baseui';
import { Input } from 'baseui/input';
import { Block } from 'baseui/block';
import useFocus from 'shared/hooks/useFocus';
import { Search } from 'baseui/icon';
import { Tag } from 'baseui/tag';
import { Popover } from 'baseui/popover';

const KEY_CODE = {
  ESC: 27,
  SLASH: 191,
};

const GlobalSearch = () => {
  const [_css, theme] = useStyletron();
  const [active, inputRefCallback, inputRef] = useFocus<HTMLInputElement>();
  const width = active ? 420 : 280;

  const focus = useCallback(() => {
    if (inputRef.current) {
      inputRef.current.focus();
    }
  }, [inputRef]);

  const blur = useCallback(() => {
    if (inputRef.current) {
      inputRef.current.blur();
    }
  }, [inputRef]);

  useEffect(() => {
    const onKeyDown = (event: KeyboardEvent) => {
      if (event.keyCode === KEY_CODE.SLASH) {
        event.stopPropagation();
        event.preventDefault();
        focus();
      } else if (event.keyCode === KEY_CODE.ESC) {
        blur();
      }
    };

    document.addEventListener('keydown', onKeyDown);

    return () => {
      document.removeEventListener('keydown', onKeyDown);
    };
  });

  const content = (
    <ul style={{ width, margin: 0 }}>
      {[1, 2, 3, 4, 5].map((item) => {
        return (
          <li
            onMouseDown={(event) => {
              event.preventDefault();
            }}
            onClick={blur}
            key={item}
            style={{
              padding: 16,
              margin: 0,
              cursor: 'pointer',
              listStyle: 'none',
            }}
          >
            item
          </li>
        );
      })}
    </ul>
  );

  return (
    <Popover isOpen={active} content={content} placement="bottomLeft">
      <Block marginLeft={theme.sizing.scale600}>
        <Input
          inputRef={inputRefCallback}
          placeholder="Search insights..."
          size="mini"
          startEnhancer={<Search />}
          endEnhancer={<Tag closeable={false}>/</Tag>}
          overrides={{
            StartEnhancer: {
              style: {
                borderTopLeftRadius: theme.sizing.scale100,
                borderBottomLeftRadius: theme.sizing.scale100,
              },
            },
            EndEnhancer: {
              style: {
                borderBottomRightRadius: theme.sizing.scale100,
                borderTopRightRadius: theme.sizing.scale100,
              },
            },
            InputContainer: {
              style: {
                width: `${width - 38}px`,
                transitionTimingFunction: 'ease',
                transitionDuration: '0.2s',
                transitionProperty: 'width',
              },
            },
          }}
        />
      </Block>
    </Popover>
  );
};

export default React.memo(GlobalSearch);
