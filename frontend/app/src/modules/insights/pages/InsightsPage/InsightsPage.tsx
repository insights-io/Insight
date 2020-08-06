import React from 'react';
import AppLayout from 'modules/app/components/AppLayout';
import { User } from '@insight/types';
import CountByCountry from 'modules/insights/components/CountByCountry';
import CountByDeviceClass from 'modules/insights/components/CountByDeviceClass';
import { Block } from 'baseui/block';
import useWindowSize from 'shared/hooks/useWindowSize';

type Props = {
  user: User;
  countByCountry: Record<string, number>;
  countByDeviceClass: Record<string, number>;
};

const InsightsPage = ({
  countByCountry,
  countByDeviceClass,
  user: _user,
}: Props) => {
  const { width = 0 } = useWindowSize();
  return (
    <AppLayout>
      <Block display="flex" flexDirection={width < 910 ? 'column' : 'row'}>
        <CountByCountry
          data={countByCountry}
          overrides={{ Root: { overrides: { Root: { style: { flex: 1 } } } } }}
        />
        <CountByDeviceClass
          data={countByDeviceClass}
          overrides={{ Root: { overrides: { Root: { style: { flex: 1 } } } } }}
        />
      </Block>
    </AppLayout>
  );
};

export default InsightsPage;
