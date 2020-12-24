import React, { useState } from 'react';
import { Tabs, Tab, TabOverrides, SharedProps } from 'baseui/tabs';
import { Drawer } from 'baseui/drawer';
import dynamic from 'next/dynamic';
import { useStyletron } from 'baseui';
import { useIsOpen } from 'shared/hooks/useIsOpen';
import { PLACEMENT, StatefulTooltip } from 'baseui/tooltip';
import { Button } from '@rebrowse/elements';
import { SIZE } from 'baseui/button';
import { FaTerminal } from 'react-icons/fa';

type Props = {
  sessionId: string;
  children: (open: () => void) => void;
};

const ConsoleTab = dynamic(() => import('developer-tools/containers/Console'));
const NetworkTab = dynamic(
  () => import('developer-tools/containers/NetworkTab')
);

type TriggerProps = {
  open: () => void;
};

const Trigger = ({ open }: TriggerProps) => {
  const label = 'Developer tools';
  return (
    <StatefulTooltip content={label} placement={PLACEMENT.bottom} showArrow>
      <Button
        size={SIZE.mini}
        onClick={open}
        aria-label={label}
        kind="secondary"
      >
        <FaTerminal size={16} />
      </Button>
    </StatefulTooltip>
  );
};

export const DeveloperTools = ({ sessionId, children }: Props) => {
  const [activeKey, setActiveKey] = useState<string | number>('0');
  const { isOpen, open, close } = useIsOpen();
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
      {children(open)}
      <Drawer
        isOpen={isOpen}
        autoFocus={false}
        renderAll={false}
        onClose={close}
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

DeveloperTools.Trigger = Trigger;
