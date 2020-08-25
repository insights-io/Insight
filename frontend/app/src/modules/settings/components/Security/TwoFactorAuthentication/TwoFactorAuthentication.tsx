import React, { useMemo, useCallback } from 'react';
import { Card } from 'baseui/card';
import { Block } from 'baseui/block';
import { useStyletron } from 'baseui';
import { APIErrorDataResponse } from '@insight/types';
import useSWR from 'swr';
import { TfaMethod, TfaSetupDTO } from '@insight/sdk/dist/auth';
import { AuthApi } from 'api';
import { isBefore } from 'date-fns';
import { Check } from 'baseui/icon';
import VerticalAligned from 'shared/components/VerticalAligned';
import FormError from 'shared/components/FormError';

import TimeBasedTwoFactorAuthentication from './TimaBasedTwoFactorAuthentication';
import SmsTwoFactorAuthentication from './SmsTwoFactorAuthentication';
import { Props } from './types';

const EMPTY_LIST: TfaSetupDTO[] = [];
const CACHE_KEY = `AuthApi.tfa.listSetups`;

const TwoFactorAuthentication = ({ user }: Props) => {
  const [css, theme] = useStyletron();
  const { data, mutate, error } = useSWR(CACHE_KEY, () =>
    AuthApi.tfa.listSetups().catch(async (errorResponse) => {
      const errorDTO: APIErrorDataResponse = await errorResponse.response.json();
      throw errorDTO.error;
    })
  );

  const loading = useMemo(() => data === undefined, [data]);
  const setups = useMemo(() => data || EMPTY_LIST, [data]);

  const enabledSince = setups.reduce((since, next) => {
    const nextDate = new Date(next.createdAt);
    if (since === undefined) {
      return nextDate;
    }
    return isBefore(nextDate, since) ? nextDate : since;
  }, undefined as Date | undefined);

  const setupsMap = useMemo(() => {
    return setups.reduce(
      (acc, setup) => ({ ...acc, [setup.method]: setup }),
      {} as Record<TfaMethod, TfaSetupDTO>
    );
  }, [setups]);

  const onMethodDisabled = useCallback(
    (method: TfaMethod) => {
      mutate(setups.filter((s) => s.method !== method));
    },
    [setups, mutate]
  );
  const onMethodEnabled = useCallback(
    (newSetup: TfaSetupDTO) => {
      mutate([...setups, newSetup]);
    },
    [setups, mutate]
  );

  const onTotpMethodDisabled = useCallback(() => onMethodDisabled('totp'), [
    onMethodDisabled,
  ]);

  const onSmsMethodDisabled = useCallback(() => onMethodDisabled('sms'), [
    onMethodDisabled,
  ]);

  let cardTitle: React.ReactNode = 'Two-factor Authentication (2FA) Methods';
  if (enabledSince) {
    cardTitle = (
      <Block display="flex" justifyContent="space-between">
        {cardTitle}{' '}
        <Block display="flex">
          <VerticalAligned>
            <Check size={24} color={theme.colors.positive} />
          </VerticalAligned>
          <span
            className={css({ fontSize: '0.8rem', color: theme.colors.mono600 })}
          >
            (enabled since {enabledSince.toLocaleDateString()})
          </span>
        </Block>
      </Block>
    );
  }

  return (
    <Card title={cardTitle}>
      <Block display="flex">
        <Block
          flex="1"
          display="flex"
          color={theme.colors.mono600}
          $style={{ fontSize: '0.8rem' }}
          marginRight={theme.sizing.scale1000}
        >
          Select your preffered method of receiving the two-factor
          authentication code. Two factor authentication adds an extra layer of
          security to your account.
        </Block>
        <Block as="ul" margin={0} padding={0}>
          <TimeBasedTwoFactorAuthentication
            setupDisabled={loading || error !== undefined}
            setupsMaps={setupsMap}
            onMethodDisabled={onTotpMethodDisabled}
            onMethodEnabled={onMethodEnabled}
          />
          <SmsTwoFactorAuthentication
            phoneNumber={user.phoneNumber}
            setupDisabled={loading || error !== undefined}
            setupsMaps={setupsMap}
            onMethodDisabled={onSmsMethodDisabled}
            onMethodEnabled={onMethodEnabled}
          />
        </Block>
      </Block>

      {error && <FormError error={error} />}
    </Card>
  );
};

export default React.memo(TwoFactorAuthentication);
