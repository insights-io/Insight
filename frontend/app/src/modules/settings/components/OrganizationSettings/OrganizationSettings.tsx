import React from 'react';
import { Block } from 'baseui/block';
import { Tabs, Tab, ORIENTATION } from 'baseui/tabs-motion';
import useOrganization from 'shared/hooks/useOrganization';
import dynamic from 'next/dynamic';
import {
  ORGANIZATION_BILLING_SETTINGS_PAGE,
  ORGANIZATION_GENERAL_SETTINGS_PAGE,
  ORGANIZATION_SECURITY_SETTINGS_PAGE,
} from 'shared/constants/routes';

import GeneralOrganizationSettings from './General';
import SecurityOrganizationSettings from './Security';

const AsyncBillingOrganizationSettings = dynamic(() => import('./Billing'), {
  ssr: false,
});

type Props = {
  activeTab: string;
  onTabChange: (key: string) => void;
};

const OrganizationSettings = ({ activeTab, onTabChange }: Props) => {
  const { organization, isLoading } = useOrganization();

  return (
    <Block>
      <Tabs
        activeKey={activeTab}
        orientation={ORIENTATION.vertical}
        onChange={(params) => onTabChange(String(params.activeKey))}
      >
        <Tab key={ORGANIZATION_GENERAL_SETTINGS_PAGE} title="General">
          <GeneralOrganizationSettings
            organization={organization}
            isLoading={isLoading}
          />
        </Tab>
        <Tab key={ORGANIZATION_SECURITY_SETTINGS_PAGE} title="Security">
          <SecurityOrganizationSettings
            organization={organization}
            isLoading={isLoading}
          />
        </Tab>
        <Tab key={ORGANIZATION_BILLING_SETTINGS_PAGE} title="Billing">
          <AsyncBillingOrganizationSettings
            organizationCreatedAt={organization?.createdAt}
          />
        </Tab>
      </Tabs>
    </Block>
  );
};

export default OrganizationSettings;
