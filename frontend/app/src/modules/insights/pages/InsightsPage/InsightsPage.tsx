import React from 'react';
import AppLayout from 'modules/app/components/AppLayout';
import { User } from '@insight/types';
import CountByCountry from 'modules/insights/components/CountByCountry';
import CountByDeviceClass from 'modules/insights/components/CountByDeviceClass';
import { Block } from 'baseui/block';
import useWindowSize from 'shared/hooks/useWindowSize';
import CountByContinent from 'modules/insights/components/CountByContinent';

type Props = {
  user: User;
  countByCountry: Record<string, number>;
  countByDeviceClass: Record<string, number>;
  countByContinent: Record<string, number>;
};

const countByCardOverrides = {
  Root: { overrides: { Root: { style: { flex: 1 } } } },
};

const InsightsPage = ({
  countByCountry,
  countByDeviceClass,
  countByContinent,
  user: _user,
}: Props) => {
  const { width = 0 } = useWindowSize();
  return (
    <AppLayout>
      <Block display="flex" flexDirection={width < 910 ? 'column' : 'row'}>
        <CountByCountry
          data={countByCountry}
          overrides={countByCardOverrides}
        />
        <CountByContinent
          data={countByContinent}
          overrides={countByCardOverrides}
        />
      </Block>

      <Block display="flex" width="100%">
        <CountByDeviceClass
          data={countByDeviceClass}
          overrides={countByCardOverrides}
        />
      </Block>
    </AppLayout>
  );
};

export default InsightsPage;
