import React, { useState, useCallback } from 'react';
import { Checkbox } from 'baseui/checkbox';
import AuthApi from 'api/auth';
import { toaster } from 'baseui/toast';
import { TfaSetupDTO } from '@insight/sdk/dist/auth';

import TimeBasedTwoFactorAuthenticationSetupModal from '../TimeBasedTwoFactorAuthenticationSetupModal';
import DisableTwoFactorAuthenticationModal from '../DisableTwoFactorAuthenticationModal';

import { Props } from './types';

const LABEL = 'Authy / Google Authenticator';

const TimeBasedTwoFactorAuthentication = ({
  setupsMaps,
  setupDisabled,
  onMethodDisabled,
  onMethodEnabled,
}: Props) => {
  const [isModalOpen, setIsModalOpen] = useState(false);

  const closeModal = useCallback(() => {
    setIsModalOpen(false);
  }, []);

  const onTotpDisable = useCallback(() => {
    AuthApi.tfa.disable('totp').then((dataResponse) => {
      if (dataResponse.data) {
        onMethodDisabled();
        closeModal();
        toaster.positive(`${LABEL} two factor authentication disabled`, {});
      }
    });
  }, [onMethodDisabled, closeModal]);

  const onTfaConfigured = (newTfaSetup: TfaSetupDTO) => {
    onMethodEnabled(newTfaSetup);
    closeModal();
    toaster.positive(`${LABEL} two factor authentication enabled`, {});
  };

  const isEnabled = setupsMaps.totp?.createdAt !== undefined;

  return (
    <li style={{ listStyleType: 'none' }}>
      <Checkbox
        checked={isEnabled}
        disabled={setupDisabled}
        onChange={() => setIsModalOpen(true)}
        overrides={{ Label: { style: { fontSize: '0.8rem' } } }}
      >
        {LABEL}
      </Checkbox>

      {isEnabled ? (
        <DisableTwoFactorAuthenticationModal
          closeModal={closeModal}
          isOpen={isModalOpen}
          header="Are you sure you want to disable two factor authentication method?"
          onConfirm={onTotpDisable}
        />
      ) : (
        <TimeBasedTwoFactorAuthenticationSetupModal
          isOpen={isModalOpen}
          onClose={closeModal}
          onTfaConfigured={onTfaConfigured}
        />
      )}
    </li>
  );
};

export default React.memo(TimeBasedTwoFactorAuthentication);
