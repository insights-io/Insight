import React, { useMemo, useState, useCallback, FormEvent } from 'react';
import { Checkbox } from 'baseui/checkbox';
import { useStyletron } from 'baseui';
import useSWR from 'swr';
import AuthApi from 'api/auth';
import { toaster } from 'baseui/toast';
import { TfaMethod, TfaSetupDTO } from '@insight/sdk/dist/auth';
import FormError from 'shared/components/FormError';
import { APIErrorDataResponse } from '@insight/types';

import TfaSetupModal from '../TfaSetupModal';

const EMPTY_LIST: TfaSetupDTO[] = [];
const CACHE_KEY = `AuthApi.tfa.listSetups`;

const TfaSetup = () => {
  const [_css, theme] = useStyletron();
  const { data, mutate, error } = useSWR(CACHE_KEY, () =>
    AuthApi.tfa.listSetups().catch(async (errorResponse) => {
      const errorDTO: APIErrorDataResponse = await errorResponse.response.json();
      throw errorDTO;
    })
  );

  const [tfaSetupModalOpen, seTtfaSetupModalOpen] = useState(false);
  const tfaSetups = useMemo(() => data || EMPTY_LIST, [data]);
  const tfaSetupsMap = useMemo(() => {
    return tfaSetups.reduce(
      (acc, setup) => ({ ...acc, [setup.method]: setup }),
      {} as Record<TfaMethod, TfaSetupDTO>
    );
  }, [tfaSetups]);
  const loadingTfaSetups = useMemo(() => data === undefined, [data]);

  const closeTfaSetupModal = useCallback(() => {
    seTtfaSetupModalOpen(false);
  }, []);

  const onTfaChange = (event: FormEvent<HTMLInputElement>) => {
    if (event.currentTarget.checked) {
      seTtfaSetupModalOpen(true);
    } else {
      AuthApi.tfa.disable('totp').then((dataResponse) => {
        if (dataResponse.data) {
          mutate(
            (data || []).filter((m) => m.method !== 'totp'),
            false
          );
          toaster.warning(
            'Two factor authentication has been successfully disabled',
            {}
          );
        }
      });
    }
  };

  const onTfaConfigured = (newTfaSetup: TfaSetupDTO) => {
    mutate([...(data || []), newTfaSetup], false);
    closeTfaSetupModal();
    toaster.positive(
      'Two factor authentication has been successfully set up',
      {}
    );
  };

  return (
    <>
      <Checkbox
        checked={tfaSetupsMap.totp?.createdAt !== undefined}
        disabled={loadingTfaSetups || error}
        onChange={onTfaChange}
      >
        Two factor authentication
        {tfaSetupsMap.totp?.createdAt && (
          <span style={{ color: theme.colors.mono600 }}>
            {' '}
            (enabled since{' '}
            {new Date(tfaSetupsMap.totp?.createdAt).toLocaleDateString()})
          </span>
        )}
      </Checkbox>
      <TfaSetupModal
        isOpen={tfaSetupModalOpen}
        onClose={closeTfaSetupModal}
        onTfaConfigured={onTfaConfigured}
      />
      {error && <FormError error={error.error} />}
    </>
  );
};

export default TfaSetup;
