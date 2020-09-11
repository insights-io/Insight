import React from 'react';
import { Block } from 'baseui/block';
import { Tabs, Tab, ORIENTATION } from 'baseui/tabs-motion';
import useOrganization from 'shared/hooks/useOrganization';

import GeneralOrganizationSettings from './General';
import SecurityOrganizationSettings from './Security';

type OrganizationSettingsKey = 'general' | 'security' | 'billing';

const OrganizationSettings = () => {
  const { organization, isLoading } = useOrganization();
  const [activeKey, setActiveKey] = React.useState<OrganizationSettingsKey>(
    'general'
  );

  return (
    <Block>
      <Tabs
        activeKey={activeKey}
        orientation={ORIENTATION.vertical}
        onChange={(params) =>
          setActiveKey(params.activeKey as OrganizationSettingsKey)
        }
      >
        <Tab key="general" title="General">
          <GeneralOrganizationSettings
            organization={organization}
            isLoading={isLoading}
          />
        </Tab>
        <Tab key="security" title="Security">
          <SecurityOrganizationSettings
            organization={organization}
            isLoading={isLoading}
          />
        </Tab>
        <Tab key="billing" title="Billing">
          TODO
        </Tab>
      </Tabs>
    </Block>
  );
};

export default OrganizationSettings;