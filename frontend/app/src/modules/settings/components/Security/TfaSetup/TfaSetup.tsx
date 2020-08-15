import React, { useMemo, useState, useCallback, FormEvent } from 'react';
import { Checkbox } from 'baseui/checkbox';
import { useStyletron } from 'baseui';
import useSWR from 'swr';
import AuthApi from 'api/auth';
import { APIErrorDataResponse } from '@insight/types';
import { toaster } from 'baseui/toast';

import TfaSetupModal from '../TfaSetupModal';

const TfaSetup = () => {
  const [_css, theme] = useStyletron();
  const { data: maybeTfaSetup, mutate, ...rest } = useSWR(
    `AuthApi.sso.tfa`,
    () =>
      AuthApi.sso.tfa().catch(async (e) => {
        const errorDTO: APIErrorDataResponse = await e.response.json();
        if (errorDTO.error.statusCode === 404) {
          return { createdAt: undefined };
        }
        throw errorDTO;
      })
  );
  const typedError = rest.error as APIErrorDataResponse | undefined;
  const isActualError = typedError && typedError.error.statusCode !== 404;
  const [tfaSetupModalOpen, seTtfaSetupModalOpen] = useState(false);

  const closeTfaSetupModal = useCallback(() => {
    seTtfaSetupModalOpen(false);
  }, []);

  const disabled = useMemo(() => {
    return (
      (maybeTfaSetup === undefined && typedError === undefined) || isActualError
    );
  }, [typedError, maybeTfaSetup, isActualError]);

  const onTfaChange = (event: FormEvent<HTMLInputElement>) => {
    if (event.currentTarget.checked) {
      seTtfaSetupModalOpen(true);
    } else {
      AuthApi.sso.tfaDisable().then((dataResponse) => {
        if (dataResponse.data) {
          mutate({ createdAt: undefined }, false);
          toaster.warning(
            'Two factor authentication has been successfully disabled',
            {}
          );
        }
      });
    }
  };

  const onTfaConfigured = (data: { createdAt: string }) => {
    mutate(data, false);
    closeTfaSetupModal();
    toaster.positive(
      'Two factor authentication has been successfully set up',
      {}
    );
  };

  return (
    <>
      <Checkbox
        checked={maybeTfaSetup?.createdAt !== undefined}
        disabled={disabled}
        onChange={onTfaChange}
      >
        Two factor authentication
        {maybeTfaSetup?.createdAt && (
          <span style={{ color: theme.colors.mono600 }}>
            {' '}
            (enabled since{' '}
            {new Date(maybeTfaSetup.createdAt).toLocaleDateString()})
          </span>
        )}
      </Checkbox>
      <TfaSetupModal
        isOpen={tfaSetupModalOpen}
        onClose={closeTfaSetupModal}
        onTfaConfigured={onTfaConfigured}
      />
    </>
  );
};

export default TfaSetup;
