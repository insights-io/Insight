import React, { useState } from 'react';
import { Tabs, Tab, TabOverrides, SharedProps } from 'baseui/tabs';
import { Drawer } from 'baseui/drawer';
import dynamic from 'next/dynamic';
import { Button, SIZE, SHAPE } from 'baseui/button';
import { useStyletron } from 'baseui';
import { StatefulTooltip } from 'baseui/tooltip';
import { FaTerminal } from 'react-icons/fa';

type Props = {
  sessionId: string;
};

const ConsoleTab = dynamic(() => import('modules/sessions/containers/Console'));
const NetworkTab = dynamic(() =>
  import('modules/sessions/containers/NetworkTab')
);

const DevTools = ({ sessionId }: Props) => {
  const [activeKey, setActiveKey] = useState<string | number>('0');
  const [isOpen, setIsOpen] = useState(false);
  const [_css, theme] = useStyletron();

  const tabOverrides: TabOverrides<SharedProps> = {
    Tab: {
      style: {
        marginLeft: 0,
        marginRight: 0,
        paddingLeft: theme.sizing.scale600,
        paddingRight: theme.sizing.scale600,
      },
    },
  };

  return (
    <>
      {!isOpen && (
        <StatefulTooltip content="DevTools" showArrow onMouseEnterDelay={1000}>
          <Button
            size={SIZE.mini}
            shape={SHAPE.pill}
            $style={{
              width: 'fit-content',
              position: 'absolute',
              top: theme.sizing.scale400,
              right: theme.sizing.scale400,
            }}
            onClick={() => setIsOpen(true)}
          >
            <FaTerminal id="devtools" />
          </Button>
        </StatefulTooltip>
      )}
      <Drawer
        isOpen={isOpen}
        autoFocus={false}
        renderAll={false}
        onClose={() => setIsOpen(false)}
        overrides={{
          Root: { style: { display: 'flex' } },
          DrawerBody: {
            style: {
              display: 'flex',
              marginLeft: 0,
              marginTop: 0,
              marginRight: 0,
              marginBottom: 0,
            },
          },
        }}
      >
        <Tabs
          renderAll={false}
          activeKey={activeKey}
          onChange={(args) => setActiveKey(args.activeKey)}
          overrides={{
            Root: { style: { width: '100%', background: '#d3d3d3' } },
            TabBar: { style: { paddingLeft: 0, paddingRight: 0 } },
            TabContent: {
              style: {
                width: '100%',
                display: 'flex',
                overflow: 'auto',
                paddingLeft: 0,
                paddingTop: 0,
                paddingRight: 0,
                paddingBottom: 0,
              },
            },
          }}
        >
          <Tab title="Console" overrides={tabOverrides}>
            <ConsoleTab sessionId={sessionId} />
          </Tab>
          <Tab title="Network" overrides={tabOverrides}>
            <NetworkTab sessionId={sessionId} />
          </Tab>
        </Tabs>
      </Drawer>
    </>
  );
};

export default React.memo(DevTools);
